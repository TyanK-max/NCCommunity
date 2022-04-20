package com.twk.nccommunity;

import com.twk.nccommunity.dao.LoginTicketMapper;
import com.twk.nccommunity.entity.LoginTicket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = NcCommunityApplication.class)
public class LoginTicketTest {
    @Autowired
    LoginTicketMapper loginTicketMapper;

    @Test
    public void test(){
        LoginTicket ticket = new LoginTicket();
        ticket.setUserId(1);
        ticket.setTicket("allow");
        ticket.setStatus(1);
        ticket.setExpired(new Date());
        loginTicketMapper.InsertLoginTicket(ticket);
        LoginTicket allow = loginTicketMapper.selectByLoginTicket("allow");
        System.out.println(allow);
        loginTicketMapper.updateStatus("allow",0);
    }
}
