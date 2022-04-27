package com.twk.nccommunity.controller;

import com.twk.nccommunity.entity.Event;
import com.twk.nccommunity.entity.Page;
import com.twk.nccommunity.entity.User;
import com.twk.nccommunity.event.EventProducer;
import com.twk.nccommunity.service.FollowService;
import com.twk.nccommunity.service.UserService;
import com.twk.nccommunity.util.CommunityConstant;
import com.twk.nccommunity.util.CommunityUtils;
import com.twk.nccommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.lang.model.element.ElementVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {
    @Autowired
    private FollowService followService;
    @Autowired
    private HostHolder holder;
    @Autowired
    private UserService userService;
    @Autowired
    private EventProducer eventProducer;

    /**
     * 关注操作
     * @param entityType 实体类型
     * @param entityId 实体ID
     */
    @RequestMapping(path = "/follow",method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user = holder.getUsers();
        followService.follow(user.getId(), entityType, entityId);
        //触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(holder.getUsers().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);
        return CommunityUtils.getJSONString(0,"关注成功!");
    }

    /**
     * 取关操作
     * @param entityType 实体类型
     * @param entityId 实体ID
     */
    @RequestMapping(path = "/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType,int entityId){
        User user = holder.getUsers();
        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtils.getJSONString(0,"取消关注成功!");
    }


    @RequestMapping(path = "/followees/{userId}",method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("用户不存在");
        }
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId,ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        ArrayList<User> users = new ArrayList<>();
        if(userList != null){
            for(Map<String,Object> map : userList){
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "site/followee";
    }

    @RequestMapping(path = "/followers/{userId}",method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId,Model model,Page page){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER,userId));

        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if(userList != null){
            for(Map<String,Object> map : userList){
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "site/follower";
    }

    private boolean hasFollowed(int userId){
        if (holder.getUsers() == null){
            return false;
        }
        return followService.hasFollowed(holder.getUsers().getId(),ENTITY_TYPE_USER,userId);
    }
}
