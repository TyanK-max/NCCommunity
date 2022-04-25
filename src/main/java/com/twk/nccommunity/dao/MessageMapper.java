package com.twk.nccommunity.dao;

import com.twk.nccommunity.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {
    //查询当前用户的所有会话,每个会话返回最新的消息
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
}
