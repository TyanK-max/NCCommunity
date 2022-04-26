package com.twk.nccommunity.controller;

import com.twk.nccommunity.annotation.LoginRequired;
import com.twk.nccommunity.entity.User;
import com.twk.nccommunity.service.FollowService;
import com.twk.nccommunity.service.LikeService;
import com.twk.nccommunity.service.UserService;
import com.twk.nccommunity.util.CommunityConstant;
import com.twk.nccommunity.util.CommunityUtils;
import com.twk.nccommunity.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder holder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;


    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "site/setting";
    }

    /**
     * 上传头像
     *
     * @param headImage 头像文件
     */
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String updateHeader(Model model, MultipartFile headImage) {
        if (headImage == null) {
            model.addAttribute("error", "您还没有选择图片!");
            return "site/setting";
        }
        String originalFilename = headImage.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "您的文件格式有误!");
            return "site/setting";
        }
        originalFilename = CommunityUtils.generateUUID() + suffix;
        File dest = new File(uploadPath + "/" + originalFilename);
        try {
            headImage.transferTo(dest);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("上传文件失败,服务器发生异常!");
        }
        //头像路径 http://localhost:8080/community/user/header/xxx.png
        User user = holder.getUsers();
        String headerUrl = domain + contextPath + "/user/header/" + originalFilename;
        userService.updateHeaderUrl(user.getId(), headerUrl);
        return "redirect:/index";
    }

    /**
     * 获取头像接口
     * @param fileName 文件路径
     */
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        fileName = uploadPath + "/" + fileName;
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        response.setContentType("image/" + suffix);
        try (
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(fileName);
        ) {
            byte[] bytes = new byte[1024];
            int b = 0;
            while ((b = fis.read(bytes)) != -1) {
                os.write(bytes, 0, b);
            }
        } catch (IOException e) {
            System.out.println("读取头像文件失败" + e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.POST)
    public String updatePwd(Model model,String oldPassword,String newPassword){
        User user = holder.getUsers();
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword);
        if(map == null || map.isEmpty()){
            model.addAttribute("msg","用户重新设置密码成功,请重新登录!");
            model.addAttribute("target","/logout");
            holder.clear();
            return "site/operate-result";
        }else{
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("newPass",newPassword);
            model.addAttribute("oldPass",oldPassword);
            return "site/setting";
        }
    }

    /**
     * 获取用户个人页面数据
     * @param userId 用户id
     */
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user",user);
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        //关注数量
        long followee = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followee);
        //粉丝数量
        long follower = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",follower);
        //是否关注
        boolean hasFollowed = false;
        if(holder.getUsers() != null){
            hasFollowed = followService.hasFollowed(holder.getUsers().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "site/profile";
    }

}
