package com.example.gobang_game.api;

import com.example.gobang_game.game.MatchRequest;
import com.example.gobang_game.game.MatchResponse;
import com.example.gobang_game.game.Mather;
import com.example.gobang_game.game.OnLineUserManager;
import com.example.gobang_game.model.User;
import com.example.gobang_game.tools.Constant;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class MatchAPI extends TextWebSocketHandler {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private OnLineUserManager onLineUserManager;

    @Autowired
    private Mather mather;

    //连接成功后做的工作
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //有可能会得到空的user，会空指针异常
        try {
            //获取当前登录玩家信息
            User user = (User) session.getAttributes().get(Constant.USERINFO_SESSION_KEY);
            //判断用户是否多开
            if (onLineUserManager.getFromGameRoom(user.getUserId()) != null ||
                    onLineUserManager.getFromGameHall(user.getUserId()) != null){
                //返回一个特殊message，供给前端判断是多开，还是页面跳转断开连接的
                MatchResponse matchResponse = new MatchResponse(true,"重复登录","repeatConnection");
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(matchResponse)));
                //session.close();
                return;
            }
            //把玩家添加到玩家管理器中，设置成在线状态
            onLineUserManager.enterGameHall(user.getUserId(),session);
            System.out.println("玩家"+ user.getUsername() + "进入游戏");
        }catch (NullPointerException e){
            //e.printStackTrace();
            System.out.println("[NatchAPI afterConnectionEstablished] 用户未登录");
            MatchResponse matchResponse = new MatchResponse(false,"您尚未登录，请先登录！",null);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(matchResponse)));

        }

    }
   //处理服务器传输的数据
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        //处理开始匹配和停止匹配请求
        User user = (User) session.getAttributes().get(Constant.USERINFO_SESSION_KEY);
        //获取客户端给服务器发送的数据
        String payload = message.getPayload();
        //把当前的json格式数据转换为java的mathRequest对象
        MatchRequest request = objectMapper.readValue(payload,MatchRequest.class);
        //处理数据并返回数据
        if (request.getMessage().equals("startMatch")){
            //进入匹配对列
            mather.add(user);
            MatchResponse matchResponse = new MatchResponse(true, null, "startMatch");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(matchResponse)));

        }else if (request.getMessage().equals("stopMatch")){
            //从队列中移除
            mather.remove(user);
            MatchResponse matchResponse = new MatchResponse(true, null, "stopMatch");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(matchResponse)));
        }else {
            MatchResponse matchResponse = new MatchResponse(true, "非法匹配请求！", null);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(matchResponse)));
        }

    }
    //出现异常后的处理
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        try {
            //获取当前登录玩家信息
            User user = (User) session.getAttributes().get(Constant.USERINFO_SESSION_KEY);
            //判断用户是否多开
            WebSocketSession temSession = onLineUserManager.getFromGameHall(user.getUserId());
            if (temSession == session){
                //把玩家从玩家管理器中删除，设置成离线状态
                onLineUserManager.exitGameHall(user.getUserId());
                mather.remove(user);
            }
        }catch (NullPointerException e) {
            e.printStackTrace();
            //MatchResponse matchResponse = new MatchResponse(false, "您尚未登录，请先登录！", null);
            //session.sendMessage(new TextMessage(objectMapper.writeValueAsString(matchResponse)));
        }
    }
    //连接关闭时的处理
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        try {
            //获取当前登录玩家信息
            User user = (User) session.getAttributes().get(Constant.USERINFO_SESSION_KEY);
            //判断用户是否多开
            WebSocketSession temSession = onLineUserManager.getFromGameHall(user.getUserId());
            if (temSession == session){
                //把玩家从玩家管理器中删除，设置成离线状态
                onLineUserManager.exitGameHall(user.getUserId());
                mather.remove(user);
            }
        } catch (NullPointerException e) {
            //e.printStackTrace();
            System.out.println("[NatchAPI afterConnectionClosed] 用户未登录");
            //MatchResponse matchResponse = new MatchResponse(false, "您尚未登录，请先登录！", null);
            //session.sendMessage(new TextMessage(objectMapper.writeValueAsString(matchResponse)));
        }
    }
}
