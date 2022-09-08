package com.example.gobang_game.game;

import com.example.gobang_game.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

@Component
public class Mather {

    //创建三个队列
    private Queue<User> normalUser = new LinkedList<>();
    private Queue<User> highUser = new LinkedList<>();
    private Queue<User> veryHighUser = new LinkedList<>();

    @Autowired
    private OnLineUserManager onLineUserManager;

    @Autowired
    private RoomManger roomManger;

    private ObjectMapper objectMapper = new ObjectMapper();

    //把玩家添加到匹配队列中
    public void add(User user){
        if (user.getScore() < 2000){
            synchronized (normalUser){
                normalUser.offer(user);
                normalUser.notify();
            }
            System.out.println(user.getUsername() + "加入到normalUser队列");
        }else if (user.getScore() >= 2000 && user.getScore() < 3000){
            synchronized (highUser){
                highUser.offer(user);
                highUser.notify();
            }
            System.out.println(user.getUsername() + "加入到highUser队列");
        }else {
            synchronized (veryHighUser){
                veryHighUser.offer(user);
                veryHighUser.notify();
            }
            System.out.println(user.getUsername() + "加入到veryHighUser队列");
        }

    }
    //把玩家从队列中移除
    public void remove(User user){
        if (user.getScore() < 2000){
            synchronized (normalUser){
                normalUser.remove(user);
            }
            System.out.println(user.getUsername() + "退出队列");
        }else if (user.getScore() >= 2000 && user.getScore() < 3000){
            synchronized (highUser){
                highUser.remove(user);
            }
            System.out.println(user.getUsername() + "退出队列");
        }else {
            synchronized (veryHighUser){
                veryHighUser.remove(user);
            }
            System.out.println(user.getUsername() + "退出队列");
        }
    }
    //创建三个线程，持续扫描三个队列
    public Mather() {
        Thread t1 = new Thread(){
            @Override
            public void run() {
              while (true){
                  handlerMatch(normalUser);
              }
            }
        };
        t1.start();

        Thread t2 = new Thread(){
            @Override
            public void run() {
                while (true){
                    handlerMatch(highUser);
                }
            }
        };
        t2.start();

        Thread t3 = new Thread(){
            @Override
            public void run() {
                while (true){
                    handlerMatch(veryHighUser);
                }
            }
        };
        t3.start();
    }

    private void handlerMatch(Queue<User> matchQueue) {
        synchronized (matchQueue){
            while (matchQueue.size() < 2){
                //队列中少于两个玩家
                try {
                    //当少于两个玩家时阻塞等待
                    matchQueue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //尝试从队列中获取两个玩家
            User player1 = matchQueue.poll();
            User player2 = matchQueue.poll();
            System.out.println("取出两个玩家"+player1.getUsername() + player2.getUsername());
            //获取取到的两个玩家的会话
            WebSocketSession session1 = onLineUserManager.getFromGameHall(player1.getUserId());
            WebSocketSession session2 = onLineUserManager.getFromGameHall(player2.getUserId());
            //  把两个玩家加入到一个游戏房间里去
            Room room = new Room();
            roomManger.add(room,player1.getUserId(), player2.getUserId());
            System.out.println(room);
            //给玩家反馈信息，即message为matchSuccess的信息，前端做相应处理
            MatchResponse response1 = new MatchResponse(true,null,"matchSuccess");
            MatchResponse response2 = new MatchResponse(true,null,"matchSuccess");
            try {
                session1.sendMessage(new TextMessage(objectMapper.writeValueAsString(response1)));
                session2.sendMessage(new TextMessage(objectMapper.writeValueAsString(response2)));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
