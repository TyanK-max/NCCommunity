package com.twk.nccommunity;

import com.twk.nccommunity.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = NcCommunityApplication.class)
public class MailTests {
    @Autowired
    MailClient mailClient;

    @Test
    public void test(){
        mailClient.sendMail("935355384@qq.com","test","Hello World!");
    }
}
