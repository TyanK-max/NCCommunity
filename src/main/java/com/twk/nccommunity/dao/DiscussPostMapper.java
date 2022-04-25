package com.twk.nccommunity.dao;

import com.twk.nccommunity.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    //查询所有评论
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId,@Param("offset") int offset,@Param("limit") int limit);
    //查询多少条评论
    int selectDiscussPostRows(@Param("userId") int userId);
    //新增一条评论
    int insertDiscussPost(DiscussPost discussPost);
    //查询一条帖子
    DiscussPost selectDiscussPostById(@Param("id") int id);
    //更新回复数量
    int updateCommentCount(@Param("id") int id,@Param("commentCount") int commentCount);
}
