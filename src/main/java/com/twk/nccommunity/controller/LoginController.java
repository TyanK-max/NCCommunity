package com.twk.nccommunity.controller;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.google.code.kaptcha.Producer;
import com.twk.nccommunity.entity.User;
import com.twk.nccommunity.service.UserService;
import com.twk.nccommunity.util.CommunityConstant;
import com.twk.nccommunity.util.CommunityUtils;
import com.twk.nccommunity.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {
    @Autowired
    private UserService userService;

    @Autowired
    private Producer producer;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage(){
        return "site/register";
    }

    @RequestMapping(path = "/forget",method = RequestMethod.GET)
    public String getForgetPage(){
        return "site/forget";
    }
    
    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "site/login";
    }

    @RequestMapping(path = "/register",method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if(map == null || map.isEmpty()){
            model.addAttribute("msg","注册成功！我们已经向您的邮箱发送了激活邮件,请您查收并尽快激活!");
            model.addAttribute("target","/index");
            return "site/operate-result";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "site/register";
        }
    }

    @RequestMapping(path = "/forget",method = RequestMethod.POST)
    public String forgotPWD(Model model, String email,String newPWD,String resetCode){
        Map<String, Object> map = userService.resetPWD(email, newPWD, resetCode);
        if (map.isEmpty()) {
            model.addAttribute("msg","重置密码成功，您的账号可以正常登录了！");
            model.addAttribute("target","/login");
            return "site/operate-result";
        }else {
            model.addAttribute("ERRMsg", map.get("ERRMsg"));
            return "site/forget";
        }
    }

    /**
     * @description: 发送验证码ctl
     * @author TyanK
     * @date 2023/4/29 11:42
     */
    @RequestMapping(path = "/resetCode/{email}",method = RequestMethod.POST)
    @ResponseBody
    public String sendEmailCode(@PathVariable("email") String email){
        Map<String, Object> map = userService.sendResetCode(email);
        if(map.isEmpty() || map == null){
            return CommunityUtils.getJSONString(0,"验证码发送成功");
        }
        return CommunityUtils.getJSONString(1,"操作失败，请检查邮箱是否输入正确",map);
    }
    
    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model,@PathVariable("userId") int userId,@PathVariable("code") String code){
        int result = userService.activation(userId, code);
        if(result == ACTIVATION_SUCCESS){
            model.addAttribute("msg","成功激活账户,您可以正常登录您的账户了！");
            model.addAttribute("target","/login");
        }else if(result == ACTIVATION_REPEAT){
            model.addAttribute("msg","激活失败,该账户已经激活过了！");
            model.addAttribute("target","/index");
        }else{
            model.addAttribute("msg","激活失败,您提供的激活码有误！");
            model.addAttribute("target","/index");
        }
        return "site/operate-result";
    }

    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/){
        String text = producer.createText();
        BufferedImage image = producer.createImage(text);
        /*验证码不再传入session中，改为redis session.setAttribute("kaptcha",text);*/
        String kaptchaOwner = CommunityUtils.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey,text,60, TimeUnit.SECONDS);
        response.setContentType("image/png");
        ServletOutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            ImageIO.write(image,"png",outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberMe,
                        Model model/*,HttpSession session*/,HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner){
//        String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if(StringUtils.isNotBlank(kaptchaOwner)) {
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = redisTemplate.opsForValue().get(kaptchaKey);
        }
        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !code.equalsIgnoreCase(kaptcha)){
            model.addAttribute("codeMsg","验证码输入有误!");
            return "site/login";
        }
        int expiredSeconds = rememberMe?REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if(map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:index";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "site/login";
        }
    }

    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}
