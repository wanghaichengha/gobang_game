package com.example.gobang_game.game;

import lombok.Data;

//表示落子亲求
@Data
public class GameRequest {
    private String message;
    private int userId;
    private int row;
    private int col;
}
