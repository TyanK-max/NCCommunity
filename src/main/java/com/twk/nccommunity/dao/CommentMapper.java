package com.twk.nccommunity.dao;

import com.twk.nccommunity.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    List<Comment> selectCommentByEntity(int entityType,int entityId,int offset,int limit);

    int selectCommentCount(int entityType,int entityId);
}
