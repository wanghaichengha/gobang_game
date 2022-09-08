package com.example.gobang_game.controller;

import com.example.gobang_game.model.User;
import com.example.gobang_game.service.UserService;
import com.example.gobang_game.tools.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@ResponseBody
public class UserController {

    @Autowired
    private UserService userService;

    //实现登录
    @RequestMapping("/login")
    public Object login(String username, String password, HttpServletRequest request){
        //查询数据库判断用户名和密码是否正确
        User user = userService.selectByUsername(username);
        if (user == null || !user.getPassword().equals(password)){
            System.out.println("登录失败！");
            //用户尚未注册或者密码错误
            return new User();
        }
        //登录成功后创建会话
        HttpSession session = request.getSession(true);
        session.setAttribute(Constant.USERINFO_SESSION_KEY,user);
        return user;

    }
    //实现注册
    @RequestMapping("/register")
    public Object register(String username,String password){
        //根据用户名在数据库查询，判断用户名是否已经占用
        User user = userService.selectByUsername(username);
        if (user != null){
            System.out.println("用户名以占用！");
            //用户尚未注册或者密码错误
            return new User();
        }
        User user1 = new User();
        user1.setUsername(username);
        user1.setPassword(password);
        int result = userService.insert(user1);
        if (result == 1){
            return user1;
        }else {
            return new User();
        }

    }
    //查询用户
    @RequestMapping("/userinfo")
    public Object getUserInfo(HttpServletRequest request){
        try{
            //获取会话,获取用户信息
            HttpSession session = request.getSession(false);
            User user = (User) session.getAttribute(Constant.USERINFO_SESSION_KEY);
            User newUser = userService.selectByUsername(user.getUsername());
            System.out.println(user);
            return newUser;
            //有可能会话为空，就会空指针异常
        }catch (NullPointerException e){
            return new User();
        }
    }

}
