package com.twk.nccommunity.dao;

import com.twk.nccommunity.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    List<Comment> selectCommentByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId,
                                        @Param("offset") int offset, @Param("limit") int limit);

    //查询评论数量
    int selectCommentCount(@Param("entityType") int entityType, @Param("entityId") int entityId);
    //添加评论
    int insertComment(Comment comment);

}
