package com.twk.nccommunity;

import com.twk.nccommunity.dao.DiscussPostMapper;
import com.twk.nccommunity.entity.DiscussPost;
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

    @Test
    public void test(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149, 0, 10);
        for(DiscussPost d:list){
            System.out.println(d);
        }
        int i = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(i);
    }
}
