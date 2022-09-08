package com.example.gobang_game.mapper;

import com.example.gobang_game.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    //根据同户名查找用户信息，用于登录
    User selectByUsername(String username);
    //往数据库user表中插入一个用户，用于注册
    int insert(User user);

    //赢家修改数据
    void userWin(int userId);

    //输家修改数据
    void loseWin(int userId);
}
