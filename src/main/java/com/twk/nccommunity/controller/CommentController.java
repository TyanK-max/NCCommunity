package com.twk.nccommunity.controller;

import com.twk.nccommunity.entity.Comment;
import com.twk.nccommunity.service.CommentService;
import com.twk.nccommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    CommentService commentService;

    @Autowired
    HostHolder holder;

    @RequestMapping(path = "/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int id, Comment comment){
        comment.setUserId(holder.getUsers().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);
        return "redirect:/discuss/detail/" + id;
    }
}
