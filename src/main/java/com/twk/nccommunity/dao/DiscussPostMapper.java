package com.twk.nccommunity.dao;

import com.twk.nccommunity.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    //查询所有评论
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId,@Param("offset") int offset,@Param("limit") int limit,@Param("orderMode") int orderMode);
    //查询多少条评论
    int selectDiscussPostRows(@Param("userId") int userId);
    //新增一条评论
    int insertDiscussPost(DiscussPost discussPost);
    //查询一条帖子
    DiscussPost selectDiscussPostById(@Param("id") int id);
    //更新回复数量
    int updateCommentCount(@Param("id") int id,@Param("commentCount") int commentCount);
    //置顶
    int updatePostType(@Param("id") int id, @Param("type") int type);
    //加精
    int updatePostStatus(@Param("id") int id,@Param("status") int status);
    //更新帖子分数
    int updatePostScore(@Param("id") int id,@Param("score") double score);
}
