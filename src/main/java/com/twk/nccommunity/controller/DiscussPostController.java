package com.twk.nccommunity.controller;

import com.twk.nccommunity.entity.*;
import com.twk.nccommunity.event.EventProducer;
import com.twk.nccommunity.service.CommentService;
import com.twk.nccommunity.service.DiscussPostService;
import com.twk.nccommunity.service.LikeService;
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

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;

    @Autowired
    LikeService likeService;

    @Autowired
    EventProducer eventProducer;
    @Autowired
    HostHolder holder;
    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user = holder.getUsers();
        if(user == null){
            return CommunityUtils.getJSONString(403,"你还没有登录哦,请先登录!");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);
        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);
        return CommunityUtils.getJSONString(0,"发布成功!");
    }

    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
    public String findDiscussPost(@PathVariable("discussPostId") int id, Model model, Page page) {
        DiscussPost post = discussPostService.findDiscussPostById(id);
        model.addAttribute("post",post);
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, id);
        model.addAttribute("likeCount",likeCount);
        //点赞状态
        int likeStatus = holder.getUsers() == null ? 0 :
                likeService.findEntityLikeStatus(holder.getUsers().getId(), ENTITY_TYPE_POST, id);
        model.addAttribute("likeStatus",likeStatus);
        page.setLimit(5);
        page.setPath("/discuss/detail/" + id);
        page.setRows(post.getCommentCount());

        //评论列表
        List<Comment> commentList = commentService
                .findCommentByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        //渲染数据列表 commentVoList
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        if(commentList != null){
            for (Comment comment:
                 commentList) {
                //评论块
                Map<String, Object> commentVo = new HashMap<>();
                //评论实体
                commentVo.put("comment", comment);
                //发表评论的用户
                commentVo.put("user",userService.findUserById(comment.getUserId()));
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount",likeCount);
                likeStatus = holder.getUsers() == null ? 0 :
                        likeService.findEntityLikeStatus(holder.getUsers().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus",likeStatus);
                //回复评论列表
                List<Comment> replyList = commentService
                        .findCommentByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //渲染回复评论数据列表
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                if(replyList != null){
                    for (Comment reply:
                         replyList) {
                        //回复评论块
                        Map<String,Object> replyVo = new HashMap<>();
                        //回复评论实体
                        replyVo.put("reply",reply);
                        //发表回复评论的用户
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        //回复的目标用户
                        User targetUser = reply.getUserId() == 0?null:userService.findUserById(reply.getTargetId());
                        replyVo.put("targetUser", targetUser);
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount",likeCount);
                        likeStatus = holder.getUsers() == null ? 0 :
                                likeService.findEntityLikeStatus(holder.getUsers().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus",likeStatus);
                        replyVoList.add(replyVo);
                    }
                    //插入上一层
                    commentVo.put("replys",replyVoList);
                    //回复评论的数量
                    int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getEntityId());
                    commentVo.put("replyCount",replyCount);
                }
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("commentVoList",commentVoList);
        return "site/discuss-detail";
    }


}
