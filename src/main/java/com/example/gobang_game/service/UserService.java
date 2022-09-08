package com.example.gobang_game.service;

import com.example.gobang_game.mapper.UserMapper;
import com.example.gobang_game.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    //根据同户名查找用户信息
    public User selectByUsername (String username){
        return userMapper.selectByUsername(username);
    }
    //往数据库user表中插入一个用户
    public int insert(User user){
        return userMapper.insert(user);
    }
    public void userWin(int userId){
        userMapper.userWin(userId);
    }
    public void loseWin(int userId){
        userMapper.loseWin(userId);
    }
}
