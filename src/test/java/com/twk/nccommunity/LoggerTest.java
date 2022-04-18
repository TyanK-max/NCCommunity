package com.twk.nccommunity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = NcCommunityApplication.class)
public class LoggerTest {
    @Test
    public void test(){
        System.out.println("This is a test!");
    }
}
