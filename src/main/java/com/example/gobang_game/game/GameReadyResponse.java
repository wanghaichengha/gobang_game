package com.example.gobang_game.game;

import lombok.Data;

//客户端连接到房间后，给客户端返回的数据
@Data
public class GameReadyResponse {
    private String message;
    private boolean ok;
    private String reason;
    private String roomId;
    private int thisUserId;
    private int thatUserId;
    private int whiteUser;

}
