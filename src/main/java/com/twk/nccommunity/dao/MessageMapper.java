package com.twk.nccommunity.dao;

import com.twk.nccommunity.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {
    //
    List<Message> selectConversations(@Param("userId") int userId,@Param("offset") int offset,@Param("limit") int limit);
    //查询当前用户会话数量
    int selectConversationCount(@Param("userId") int userId);
    //查询某个会话包含的所有信息
    List<Message> selectLetters(@Param("conversationId") String conversationId,@Param("offset") int offset,@Param("limit") int limit);
    //查询某个会话包含的私信数量
    int selectLetterCount(@Param("conversationId") String conversationId);
    //查询未读私信数量
    int selectLetterUnreadCount(@Param("userId") int userId,@Param("conversationId") String conversationId);
    //新增消息
    int insertMessage(Message message);
    //消息已读或删除
    int updateStatus(@Param("ids") List<Integer> ids,@Param("status") int status);
    //查询某个主题下最新的系统通知
    Message selectLatestNotice(@Param("userId") int userId,@Param("topic") String topic);
    //查询某个主题包含的通知数量
    int selectNoticeCount(@Param("userId")int userId,@Param("topic") String topic);
    //查询某个主题未读通知数量
    int selectNoticeUnreadCount(@Param("userId")int userId,@Param("topic") String topic);
    //查询某个主题的通知列表
    List<Message> findNotice(@Param("userId")int userId,@Param("topic") String topic,@Param("offset") int offset,@Param("limit") int limit);
}
