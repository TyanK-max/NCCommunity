package com.twk.nccommunity.controller;

import com.twk.nccommunity.entity.User;
import com.twk.nccommunity.service.LikeService;
import com.twk.nccommunity.util.CommunityUtils;
import com.twk.nccommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

@Controller
public class LikeController {
    @Autowired
    LikeService likeService;
    @Autowired
    HostHolder holder;

    @RequestMapping(path = "/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId){
        User user = holder.getUsers();
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);
        return CommunityUtils.getJSONString(0,null,map);
    }
}
