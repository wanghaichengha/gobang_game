package com.example.gobang_game.game;

import lombok.Data;

@Data
public class MatchResponse {
    private boolean ok;
    private String reason;
    private String message;


    public MatchResponse(boolean ok, String reason, String message) {
        this.ok = ok;
        this.reason = reason;
        this.message = message;
    }
}
