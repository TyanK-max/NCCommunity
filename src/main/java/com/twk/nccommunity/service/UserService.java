package com.twk.nccommunity.service;

import com.twk.nccommunity.dao.UserMapper;
import com.twk.nccommunity.entity.DiscussPost;
import com.twk.nccommunity.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    UserMapper userMapper;

     public User findPostById(int userId){
         return userMapper.selectById(userId);
     }

    public User findPostByEmail(String email){
        return userMapper.selectByEmail(email);
    }

    public User findPostByName(String username){
        return userMapper.selectByName(username);
    }
}
