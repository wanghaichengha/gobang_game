package com.example.gobang_game.config;

import com.example.gobang_game.api.GameAPI;
import com.example.gobang_game.api.MatchAPI;
import com.example.gobang_game.api.TextAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private TextAPI textAPI;

    @Autowired
    private MatchAPI matchAPI;

    @Autowired
    private GameAPI gameAPI;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(textAPI,"/text");
        registry.addHandler(matchAPI,"/findMatch")
                //把httpSession会话拿到webSocketSession中
                .addInterceptors(new HttpSessionHandshakeInterceptor());
        registry.addHandler(gameAPI,"/game")
                //把httpSession会话拿到webSocketSession中
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    }


}
