package com.twk.nccommunity.dao;

import com.twk.nccommunity.entity.LoginTicket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoginTicketMapper {

    int InsertLoginTicket(LoginTicket loginTicket);

    LoginTicket selectByLoginTicket(@Param("ticket") String ticket);

    int updateStatus(@Param("ticket") String ticket,@Param("status") int status);
}
