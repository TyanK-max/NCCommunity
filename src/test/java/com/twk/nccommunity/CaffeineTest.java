package com.twk.nccommunity;

import com.twk.nccommunity.entity.DiscussPost;
import com.twk.nccommunity.service.DiscussPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.Random;

@SpringBootTest
@ContextConfiguration(classes = NcCommunityApplication.class)
public class CaffeineTest {
    @Autowired
    private DiscussPostService discussPostService;

    @Test
    public void initDataForStress(){
        for(int i = 0; i <300000 ;i++){
            DiscussPost post = new DiscussPost();
            post.setUserId(111);
            post.setTitle("压力测试数据");
            post.setContent("此为压力测试数据,大致300000条重复数据");
            post.setCreateTime(new Date());
            post.setScore(Math.random() * 2000);
            discussPostService.addDiscussPost(post);
        }
    }

    @Test
    public void test(){
        System.out.println(discussPostService.findDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.findDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.findDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.findDiscussPosts(0, 0, 10, 0));
    }
}
