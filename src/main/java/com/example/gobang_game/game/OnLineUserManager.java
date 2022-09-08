package com.example.gobang_game.game;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnLineUserManager {
    //1,管理在线玩家的信息
    private ConcurrentHashMap<Integer, WebSocketSession> gameHall = new ConcurrentHashMap<>();
    //玩家进入游戏，将玩家信息添加到map中
    public void enterGameHall(int userId,WebSocketSession webSocketSession){
        gameHall.put(userId,webSocketSession);
    }
    //玩家退出游戏，从map中删除玩家信息
    public void exitGameHall(int userId){
        gameHall.remove(userId);
    }
    //获取在线玩家信息
    public WebSocketSession getFromGameHall(int userId){
        return gameHall.get(userId);
    }

    //2,管理进去房间里的玩家
    private ConcurrentHashMap<Integer, WebSocketSession> gameRoom = new ConcurrentHashMap<>();
    //玩家进入房间，将玩家信息添加到map中
    public void enterGameRoom(int userId,WebSocketSession webSocketSession){
        gameRoom.put(userId,webSocketSession);
    }
    //玩家退出游戏，从map中删除玩家信息
    public void exitGameRoom(int userId){
        gameRoom.remove(userId);
    }
    //获取在线玩家信息
    public WebSocketSession getFromGameRoom(int userId){
        return gameRoom.get(userId);
    }
}
