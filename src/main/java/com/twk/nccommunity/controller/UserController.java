package com.twk.nccommunity.controller;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.twk.nccommunity.annotation.LoginRequired;
import com.twk.nccommunity.entity.Comment;
import com.twk.nccommunity.entity.DiscussPost;
import com.twk.nccommunity.entity.Page;
import com.twk.nccommunity.entity.User;
import com.twk.nccommunity.service.*;
import com.twk.nccommunity.util.CommunityConstant;
import com.twk.nccommunity.util.CommunityUtils;
import com.twk.nccommunity.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.jws.WebParam;
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

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @Value("${qiniu.key.acess}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model) {
        // 生成头像别名
        String fileName = CommunityUtils.generateUUID();
        // 设置callback
        StringMap putPolicy = new StringMap();
        putPolicy.put("returnBody",CommunityUtils.getJSONString(0));
        // 生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        long expiresSeconds = 3600;
        String token = auth.uploadToken(headerBucketName, fileName, expiresSeconds, putPolicy);
        model.addAttribute("uploadToken",token);
        model.addAttribute("fileName",fileName);
        return "site/setting";
    }

    /**
     * @description: 更新用户头像
     * @date 2022/11/9 9:38
     */
    @RequestMapping(path = "/header/url",method = RequestMethod.POST)
    @ResponseBody
    @CrossOrigin
    public String updateHeaderUrl(String fileName){
        if(StringUtils.isBlank(fileName)){
            return CommunityUtils.getJSONString(1,"文件名不能为空!");
        }
        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeaderUrl(holder.getUsers().getId(),url);
        return CommunityUtils.getJSONString(0);
    }
    
    /**
     * 本地上传头像
     *
     * @param headImage 头像文件
     */
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    @Deprecated
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
     *
     * @param fileName 文件路径
     */
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    @Deprecated
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
    @RequestMapping(path = "/setting", method = RequestMethod.POST)
    public String updatePwd(Model model, String oldPassword, String newPassword) {
        User user = holder.getUsers();
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "用户重新设置密码成功,请重新登录!");
            model.addAttribute("target", "/logout");
            holder.clear();
            return "site/operate-result";
        } else {
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("newPass", newPassword);
            model.addAttribute("oldPass", oldPassword);
            return "site/setting";
        }
    }

    /**
     * 获取用户个人页面数据
     *
     * @param userId 用户id
     */
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        if(holder.getUsers() == null){
            return "redirect:/login";
        }
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        //关注数量
        long followee = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followee);
        //粉丝数量
        long follower = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", follower);
        //是否关注
        boolean hasFollowed = false;
        if (holder.getUsers() != null) {
            hasFollowed = followService.hasFollowed(holder.getUsers().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "site/profile";
    }

    /**
     * @description: 获取用户自己写的帖子
     * @author TyanK
     * @date 2022/11/6 17:42
     */
    @RequestMapping(path = "/profile/postList/{userId}", method = RequestMethod.GET)
    public String getOwnerPost(@PathVariable("userId") int userId, Model model, Page page) {
        int size = discussPostService.findDiscussPostRows(userId);
        page.setRows(size);
        page.setPath("/profile/postList/" + userId);
        User user = userService.findUserById(userId);
        List<DiscussPost> postList = discussPostService.findDiscussPostByUserId(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> postMap = new ArrayList<>();
        if (postList != null) {
            for (DiscussPost post : postList) {
                Map<String, Object> map = new HashMap<>();
                long likeCnt = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("like", likeCnt);
                map.put("post", post);
                postMap.add(map);
            }
        }
        model.addAttribute("user", user);
        model.addAttribute("postCnt", size);
        model.addAttribute("discussPosts", postMap);
        return "site/my-post";
    }

    @RequestMapping(path = "/profile/repList/{userId}", method = RequestMethod.GET)
    public String getOwnerComment(@PathVariable("userId") int userId, Model model, Page page) {
        int size = commentService.findOwnerCommentCount(userId);
        page.setRows(size);
        page.setPath("/user/profile/repList/" + userId);
        User user = userService.findUserById(userId);
        List<Comment> commentList = commentService.findCommentByUser(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentMap = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                HashMap<String, Object> map = new HashMap<>();
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                int type = comment.getEntityType();
                int postId = 0;
                Object entity = null;
                if (type == 1) {
                    postId = comment.getEntityId();
                    entity = discussPostService.findDiscussPostById(postId);
                } else if (type == 2) {
                    int commentId = comment.getEntityId();
                    Comment com = commentService.findCommentById(commentId);
                    postId = com.getEntityId();
                    entity = com;
                }
                map.put("like", likeCount);
                map.put("comment", comment);
                map.put("postId", postId);
                map.put("entity", entity);
                map.put("type", type);
                commentMap.add(map);
            }
        }
        model.addAttribute("user", user);
        model.addAttribute("comCnt", size);
        model.addAttribute("comments", commentMap);
        return "site/my-reply";
    }
    
    @RequestMapping(path = "/userInfo/{userId}",method = RequestMethod.POST)
    @ResponseBody
    public String getUserById(@PathVariable("userId") int userId){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new IllegalStateException("无法查到到该用户");
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("userName",user.getUsername());
        return CommunityUtils.getJSONString(0,"success",map);
    }
    
}
