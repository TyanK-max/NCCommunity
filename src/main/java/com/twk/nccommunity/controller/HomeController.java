package com.twk.nccommunity.controller;

import com.twk.nccommunity.dao.DiscussPostMapper;
import com.twk.nccommunity.entity.DiscussPost;
import com.twk.nccommunity.entity.Page;
import com.twk.nccommunity.entity.User;
import com.twk.nccommunity.service.DiscussPostService;
import com.twk.nccommunity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;

    @RequestMapping(path = "/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");
        List<DiscussPost> posts = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(posts!=null){
            for(DiscussPost post:posts){
                Map<String, Object> map = new HashMap<>();
                map.put("posts",post);
                User user = userService.findPostById(post.getUserId());
                map.put("user",user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        return "index";
    }
}
