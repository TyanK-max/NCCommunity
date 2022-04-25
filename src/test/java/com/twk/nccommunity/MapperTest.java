package com.twk.nccommunity;

import com.twk.nccommunity.dao.DiscussPostMapper;
import com.twk.nccommunity.dao.MessageMapper;
import com.twk.nccommunity.entity.DiscussPost;
import com.twk.nccommunity.entity.Message;
import org.apache.ibatis.annotations.Param;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = NcCommunityApplication.class)
public class MapperTest {
    @Autowired
    DiscussPostMapper discussPostMapper;

    @Autowired
    MessageMapper messageMapper;
    @Test
    public void test(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149, 0, 10);
        for(DiscussPost d:list){
            System.out.println(d);
        }
        int i = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(i);
    }

    @Test
    public void testMessage(){
        List<Message> messages = messageMapper.selectConversations(111, 0, 20);
        for (Message m:
             messages) {
            System.out.println(m);
        }
        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        messages = messageMapper.selectLetters("111_112",0,10);
        for (Message m:
             messages) {
            System.out.println(m);
        }

        int count1 = messageMapper.selectLetterCount("111_112");
        System.out.println(count1);


        int count2 = messageMapper.selectLetterUnreadCount(131,"111_131");
        System.out.println(count2);

    }
}
