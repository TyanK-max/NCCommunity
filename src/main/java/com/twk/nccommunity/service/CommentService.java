package com.twk.nccommunity.service;

import com.twk.nccommunity.dao.CommentMapper;
import com.twk.nccommunity.entity.Comment;
import com.twk.nccommunity.util.CommunityConstant;
import com.twk.nccommunity.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;
import org.unbescape.html.HtmlEscape;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    SensitiveFilter sensitiveFilter;

    @Autowired
    DiscussPostService discussPostService;

    public List<Comment> findCommentByEntity(int entityType,int entityId,int offset,int limit){
        return commentMapper.selectCommentByEntity(entityType,entityId,offset,limit);
    }
    public int findCommentCount(int entityType,int entityId){
        return commentMapper.selectCommentCount(entityType,entityId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if(comment == null){
            throw new IllegalArgumentException("评论内容不能为空!");
        }
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.goToFilter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            int count = commentMapper.selectCommentCount(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }
        return rows;
    }

    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }
    
    public int findOwnerCommentCount(int userId){
        return commentMapper.selectCommentRowsByUser(userId);
    }
    
    public List<Comment> findCommentByUser(int userId,int offset,int limit){
        return commentMapper.selectCommentByUser(userId, offset, limit);
    }
    
    public int deleteCommentWhenDelPost(int entityId,int entityType){
        return commentMapper.deleteCommentByPostId(entityId,entityType);
    }
}
