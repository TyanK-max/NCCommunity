package com.twk.nccommunity;

import com.twk.nccommunity.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = NcCommunityApplication.class)
public class SensitiveTest {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void test(){
        System.out.println(sensitiveFilter.goToFilter("滚你妈的,臭傻吊,干&你#妈￥死了!"));
    }
}
