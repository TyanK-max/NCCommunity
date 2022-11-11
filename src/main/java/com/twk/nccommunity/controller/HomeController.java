package com.twk.nccommunity.controller;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.twk.nccommunity.dao.DiscussPostMapper;
import com.twk.nccommunity.entity.DiscussPost;
import com.twk.nccommunity.entity.Page;
import com.twk.nccommunity.entity.User;
import com.twk.nccommunity.service.DiscussPostService;
import com.twk.nccommunity.service.LikeService;
import com.twk.nccommunity.service.UserService;
import com.twk.nccommunity.util.CommunityConstant;
import com.twk.nccommunity.util.CommunityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;

    @Value("${qiniu.key.acess}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;
    
    @Value("${qiniu.bucket.file.name}")
    private String fileBucketName;

    @Value(("${qiniu.bucket.file.url}"))
    private String fileBucketUrl;
    
    @RequestMapping(path = "/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page,
                               @RequestParam(name = "orderMode",defaultValue = "0") int orderMode){
        //orderMode: 0：最新版块，1：最热版块 caffeine 默认只存储最新的
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode=" + orderMode);
        List<DiscussPost> posts = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(),orderMode);
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(posts!=null){
            for(DiscussPost post:posts){
                Map<String, Object> map = new HashMap<>();
                map.put("posts",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount",likeCount);
                discussPosts.add(map);
            }
        }
        // 生成文件别名
        String fileUUID = CommunityUtils.generateUUID();
        // 设置callback
        StringMap putPolicy = new StringMap();
        putPolicy.put("returnBody",CommunityUtils.getJSONString(0));
        // 生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        long expiresSeconds = 3600;
        String token = auth.uploadToken(fileBucketName, fileUUID, expiresSeconds, putPolicy);
        model.addAttribute("uploadToken",token);
        model.addAttribute("fileName",fileUUID);
        
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("orderMode",orderMode);
        return "index";
    }

    @RequestMapping(path = "/error",method = RequestMethod.GET)
    public String getErrorPage(){
        return "error/500";
    }

    @RequestMapping(path = "/denied",method = RequestMethod.GET)
    public String getDeniedPage(){
        return "error/404";
    }
}
