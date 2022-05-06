package com.twk.nccommunity.controller;

import com.twk.nccommunity.entity.Comment;
import com.twk.nccommunity.entity.DiscussPost;
import com.twk.nccommunity.entity.Event;
import com.twk.nccommunity.event.EventProducer;
import com.twk.nccommunity.service.CommentService;
import com.twk.nccommunity.service.DiscussPostService;
import com.twk.nccommunity.util.CommunityConstant;
import com.twk.nccommunity.util.HostHolder;
import com.twk.nccommunity.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {
    @Autowired
    CommentService commentService;
    @Autowired
    DiscussPostService discussPostService;
    @Autowired
    EventProducer eventProducer;
    @Autowired
    HostHolder holder;
    @Autowired
    RedisTemplate redisTemplate;

    @RequestMapping(path = "/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int id, Comment comment){
        comment.setUserId(holder.getUsers().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);
        //触发评论事件
        Event event = new Event()
                        .setTopic(TOPIC_COMMENT)
                        .setUserId(holder.getUsers().getId())
                        .setEntityType(comment.getEntityType())
                        .setEntityId(comment.getEntityId())
                        .setData("postId",id);
        //计算帖子分数
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey,id);
        //判断实体类型，从而取得目标用户ID，获得其发布的实体，实现链接
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }else if(comment.getEntityType() == ENTITY_TYPE_COMMENT){
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);

        //新增评论的话需要更新一下elasticsearch里的数据
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(id);
            eventProducer.fireEvent(event);
        }
        return "redirect:/discuss/detail/" + id;
    }
}
