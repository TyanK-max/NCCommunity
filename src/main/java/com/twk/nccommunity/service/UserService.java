package com.twk.nccommunity.service;

import com.twk.nccommunity.dao.LoginTicketMapper;
import com.twk.nccommunity.dao.UserMapper;
import com.twk.nccommunity.entity.DiscussPost;
import com.twk.nccommunity.entity.LoginTicket;
import com.twk.nccommunity.entity.User;
import com.twk.nccommunity.util.CommunityConstant;
import com.twk.nccommunity.util.CommunityUtils;
import com.twk.nccommunity.util.MailClient;
import com.twk.nccommunity.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import sun.swing.StringUIClientPropertyKey;

import java.util.*;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

//    @Autowired
//    LoginTicketMapper loginTicketMapper;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int userId) {
        User cache = getCache(userId);
        if(cache == null){
            cache = initCache(userId);
        }
        return cache;
    }

    public User findUserByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    /**
     * 注册用户功能
     * @param user 一个用户对象
     */
    public Map<String, Object> register(User user) {
        HashMap<String, Object> map = new HashMap<>();
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "用户名不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }
        //用户名唯一
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该用户名已经被注册!");
            return map;
        }
        //邮箱唯一
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册!");
        }
        //注册用户
        user.setSalt(UUID.randomUUID().toString().substring(0, 5));
        user.setPassword(CommunityUtils.md5(user.getPassword() + user.getSalt()));
        user.setStatus(0);
        user.setType(0);
        user.setActivationCode(UUID.randomUUID().toString());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //发送邮箱
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活用户邮件", content);

        return map;
    }

    /**
     * 激活用户功能
     *
     * @param userId 用户ID
     * @param code   用户邮件验证码
     */
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * 验证登录并获取登陆凭证功能
     *
     * @param username       用户名
     * @param password       密码
     * @param expiredSeconds 登陆凭证有效期
     */
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该用户不存在!");
            return map;
        }
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该用户未激活!");
            return map;
        }
        if (!user.getPassword().equals(CommunityUtils.md5(password + user.getSalt()))) {
            map.put("passwordMsg", "用户密码输入不正确");
            return map;
        }
        LoginTicket ticket = new LoginTicket();
        ticket.setUserId(user.getId());
        ticket.setTicket(CommunityUtils.generateUUID());
        ticket.setStatus(0);
        ticket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        String ticketKey = RedisKeyUtil.getTicketKey(ticket.getTicket());
        redisTemplate.opsForValue().set(ticketKey,ticket);
        map.put("ticket", ticket.getTicket());
        return map;
    }

    /**
     * 登出功能
     * @param ticket 登录凭证
     */
    public void logout(String ticket) {
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey,loginTicket);
    }

    /**
     * 查找登录凭证
     *
     * @param ticket 凭证
     * @return LoginTicket 对象
     */
    public LoginTicket findLoginTicket(String ticket) {
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        return loginTicket;
    }

    /**
     * 更新用户头像
     * @param userId 用户id
     * @param headerUrl 用户头像地址
     */
    public int updateHeaderUrl(int userId,String headerUrl){
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    /**
     * 重新设置密码
     * @param userId id
     * @param oldPassword 原密码
     * @param newPassword 新密码
     */
    public Map<String,Object> updatePassword(int userId,String oldPassword,String newPassword){
        Map<String,Object> map = new HashMap<>();
        User user = userMapper.selectById(userId);
        oldPassword = CommunityUtils.md5(oldPassword + user.getSalt());
        if(oldPassword == null || newPassword == null){
            map.put("passwordMsg","密码输入不能为空!");
            return map;
        }
        if(!oldPassword.equals(user.getPassword())){
            map.put("passwordMsg","输入密码与原密码不符,请您重新输入!");
            return map;
        }
        newPassword = CommunityUtils.md5(newPassword + user.getSalt());
        int rows = userMapper.updatePassword(userId, newPassword);
        if(rows != 0) clearCache(userId);
        return map;
    }

    /**
     * 通过名字查找用户
     * @param username 用户名
     */
    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    /**
     * 获取用户数据缓存
     * @param userId 用户ID
     * @return
     */
    public User getCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    /**
     * 初始化用户缓存
     * @param userId 用户ID
     */
    public User initCache(int userId){
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey,user);
        return user;
    }

    /**
     * 当用户数据更新是，删除Redis中的缓存
     * @param userId 用户ID
     */
    public void clearCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = this.findUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1: return AUTHORITY_ADMIN;
                    case 2: return AUTHORITY_MODERATOR;
                    default: return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
