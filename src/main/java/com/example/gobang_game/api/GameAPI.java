package com.example.gobang_game.api;

import com.example.gobang_game.game.*;
import com.example.gobang_game.model.User;
import com.example.gobang_game.service.UserService;
import com.example.gobang_game.tools.Constant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

@Component
public class GameAPI extends TextWebSocketHandler {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RoomManger roomManger;

    @Autowired
    private OnLineUserManager onLineUserManager;

    @Autowired
    private UserService userService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        GameReadyResponse response = new GameReadyResponse();

        //获取用户会话判断是否登录了
        User user = (User) session.getAttributes().get(Constant.USERINFO_SESSION_KEY);
        if (user ==null){
            response.setOk(false);
            response.setReason("用户尚未登录！");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            return;
        }
        //判断用户是否已经进入房间
        Room room = roomManger.getRoomByUserId(user.getUserId());
        if (room == null){
            //为空，表示当前玩家还未进去任何房间
            response.setOk(false);
            response.setReason("用户尚未登录！");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            return;
        }
        //判断当前用户是否多开
        //一个在游戏房间一个在游戏大厅也算多开
        if (onLineUserManager.getFromGameRoom(user.getUserId()) != null ||
                onLineUserManager.getFromGameHall(user.getUserId()) != null){
            response.setOk(false);
            response.setReason("禁止多开！");
            response.setMessage("repeatConnection");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            return;
        }
        //把用户加入到房间管理器中
        onLineUserManager.enterGameRoom(user.getUserId(),session);
        System.out.println(user);
        //把两个玩家加入到房间中
        //两个用户进入同一个房间，修改同一个房间里的数据，存在线程安全问题
        synchronized (room){
            if (room.getUser1() == null){
                room.setUser1(user);
                //把第一个进入游戏房间的设置为先手方
                room.setWhiteUser(user.getUserId());
                System.out.println(user.getUsername()+"作为第一个玩家加入游戏");
                return;
            }
            if (room.getUser2() == null){
                room.setUser2(user);
                System.out.println(user.getUsername()+"作为第二个玩家加入游戏");
                //当两个玩家都进入房间后给两个玩家返回数据
                //通知玩家1
                noticeGameReady(room,room.getUser1(),room.getUser2());
                //通知玩家2
                noticeGameReady(room,room.getUser2(),room.getUser1());
                return;
            }
        }
        //第三个用户要是也加入到这个房间，就给客户端返回信息
        response.setOk(false);;
        response.setMessage("当前房间已满，无法加入！");
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));

    }

    private void noticeGameReady(Room room, User thisUser, User thatUser) {
        GameReadyResponse response = new GameReadyResponse();
        response.setMessage("gameReady");
        response.setOk(true);
        response.setReason("");
        response.setRoomId(room.getRoomId());
        response.setThatUserId(thatUser.getUserId());
        response.setThisUserId(thisUser.getUserId());
        response.setWhiteUser(room.getWhiteUser());
        WebSocketSession session = onLineUserManager.getFromGameRoom(thisUser.getUserId());
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //处理亲求
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
         User user = (User) session.getAttributes().get(Constant.USERINFO_SESSION_KEY);
         if (user ==null){
             System.out.println("用户未登录！");
             return;
         }
         //根据玩家id获取房间对象
        Room room = roomManger.getRoomByUserId(user.getUserId());
         //通过room对象处理请求
         room.putChess(message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        User user = (User) session.getAttributes().get(Constant.USERINFO_SESSION_KEY);
        if (user == null) {
            return;
        }
        //判断用户是否多开，防止第二个用户退出并从房间管理器中删除，导致第一个用户也被删除
        WebSocketSession exitSession = onLineUserManager.getFromGameRoom(user.getUserId());
        if (session == exitSession) {
            onLineUserManager.exitGameRoom(user.getUserId());
        }
        System.out.println(user.getUserId() + "玩家连接异常！");
        //如果一方掉线，通知另一方赢
        noticeThatUserWin(user);

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        User user = (User) session.getAttributes().get(Constant.USERINFO_SESSION_KEY);
        if (user == null){
            return;
        }
        //判断用户是否多开，防止第二个用户退出并从房间管理器中删除，导致第一个用户也被删除
        WebSocketSession exitSession = onLineUserManager.getFromGameRoom(user.getUserId());
        if (session == exitSession){
            onLineUserManager.exitGameRoom(user.getUserId());
        }
        System.out.println(user.getUserId()+"玩家离开房间！");
        //如果一方掉线，通知另一方赢
        noticeThatUserWin(user);
    }

    private void noticeThatUserWin(User user) throws IOException {
        Room room = roomManger.getRoomByUserId(user.getUserId());
        if (room == null){
            //说明对手也掉线了，无需通知对手
            System.out.println("房间已被销毁");
            return;
        }

        //根据房间找到对手
        User thatUser = (user == room.getUser1()? room.getUser2() : room.getUser1());
        //找到对手在线状态
        WebSocketSession session = onLineUserManager.getFromGameRoom(thatUser.getUserId());
        if (session == null){
            System.out.println("对手已经掉线！");
            return;
        }
        GameResponse response = new GameResponse();
        response.setUserId(thatUser.getUserId());
        response.setMessage("putChess");
        response.setWinner(thatUser.getUserId());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        //胜负以分，修改用户数据库分数数据
        int winUserId = thatUser.getUserId();
        int loseUserId = user.getUserId();
        userService.userWin(winUserId);
        userService.loseWin(loseUserId);
        //释放房间
        roomManger.remove(room.getRoomId(), room.getUser1().getUserId(),room.getUser2().getUserId());
    }
}
