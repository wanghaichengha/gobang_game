package com.example.gobang_game.game;

import com.example.gobang_game.GobangGameApplication;
import com.example.gobang_game.model.User;
import com.example.gobang_game.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.UUID;

@Data
public class Room {
    private String roomId;
    private User user1;
    private User user2;
    //先手方玩家id
    private int whiteUser;

    //用一个数组记录棋子位置
    //约定玩家一落子位置为1，玩家二落子位置为2，空位置为0
    private int[][] board = new int[15][15];

    //@Autowired
    private OnLineUserManager onLineUserManager;
    //引入roomManger用来房间销毁
    //@Autowired
    private RoomManger roomManger;

    private UserService userService;

    ObjectMapper objectMapper = new ObjectMapper();

    public Room() {
        roomId = UUID.randomUUID().toString();
        //手动注入OnLineUserManager
        onLineUserManager = GobangGameApplication.context.getBean(OnLineUserManager.class);
        roomManger = GobangGameApplication.context.getBean(RoomManger.class,
        userService = GobangGameApplication.context.getBean(UserService.class));
    }
    //需要做一系列的处理
    public void putChess(String reqJson) throws IOException {
        //1，记录棋子位置
        GameRequest request = objectMapper.readValue(reqJson,GameRequest.class);
        //判断当前是谁落子，然后记录在数组中
        int chess = (request.getUserId() == user1.getUserId()? 1 : 2);
        int row = request.getRow();
        int col = request.getCol();
        if (board[row][col] !=0 ){
            System.out.println("当前位置已有棋子！");
            return;
        }
        board[row][col] = chess;
        //2，打印棋盘，便于之后的判断胜负
        printBoard();
        //3，进行胜负判定
        int winner = checkWinner(row,col,chess);
        //4，给客户端返回数据
        GameResponse response = new GameResponse();
        response.setMessage("putChess");
        response.setUserId(request.getUserId());
        response.setRow(row);
        response.setCol(col);
        response.setWinner(winner);

        WebSocketSession session1 = onLineUserManager.getFromGameRoom(user1.getUserId());
        WebSocketSession session2 = onLineUserManager.getFromGameRoom(user2.getUserId());
        //5,处理两个玩家突然掉线，判定对方获胜的情况
        if (session1 == null){
            //玩家一掉线
            response.setWinner(user2.getUserId());
            System.out.println("玩家"+user1.getUsername()+"掉线");
        }
        if (session2 == null){
            //玩家一掉线
            response.setWinner(user1.getUserId());
            System.out.println("玩家"+user2.getUsername()+"掉线");
        }
        String respJson = objectMapper.writeValueAsString(response);
        if (session1 != null){
            session1.sendMessage(new TextMessage(respJson));
        }
        if (session2 != null){
            session2.sendMessage(new TextMessage(respJson));
        }

        //6,当胜负已分，就把房间销毁
        if (response.getWinner() != 0){
            System.out.println("胜负已分，销毁roomId:"+roomId+"房间");
            //胜负以分后，修改数据库玩家分数等
            int winUserId = response.getWinner();
            int loseUserId = (response.getWinner() == user1.getUserId()? user2.getUserId():user1.getUserId());
            userService.userWin(winUserId);
            userService.loseWin(loseUserId);

            roomManger.remove(roomId,user1.getUserId(),user2.getUserId());
        }
    }

    private void printBoard() {
        //打印棋盘
        System.out.println("打印棋盘");
        System.out.println("================================================================================");
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                System.out.print(board[i][j]);
            }
            System.out.println();
        }
        System.out.println("================================================================================");
    }


    private int checkWinner(int row, int col,int chess) {
        //谁赢了就给前端返回谁的userId
        //1,先检查落子所在行
        for (int c = col - 4; c <= col; c++) {
            try {
                if (board[row][c] == chess
                        && board[row][c + 1] == chess
                        && board[row][c + 2] == chess
                        && board[row][c + 3] == chess
                        && board[row][c + 4] == chess) {
                    //胜负以分
                    return chess == 1 ? user1.getUserId() : user2.getUserId();
                }
            }catch (ArrayIndexOutOfBoundsException e){
                continue;
            }

        }
        //2,检查落子所在列
        for (int r = row - 4; r <= row; r++) {
            try {
                if (board[r][col] == chess
                        && board[r+1][col] == chess
                        && board[r+2][col] == chess
                        && board[r+3][col] == chess
                        && board[r+4][col] == chess) {
                    //胜负以分
                    return chess == 1 ? user1.getUserId() : user2.getUserId();
                }
            }catch (ArrayIndexOutOfBoundsException e){
                continue;
            }
        }
        //3,检查左对角线
        for (int r = row-4 , c = col-4; r <= row && c <= col; r++ , c++) {
            try {
                if (board[row][col] == chess
                  && board[row+1][col+1] ==chess
                  && board[row+2][col+2] ==chess
                  && board[row+3][col+3] ==chess
                  && board[row+4][col+4] ==chess
                )
            //胜负以分
             return chess == 1 ? user1.getUserId() : user2.getUserId();
            }catch (ArrayIndexOutOfBoundsException e){
                continue;
            }
        }
        //4,检查又对角线
        for (int r = row-4 , c = col+4; r <= row && c >= col; r++ , c--) {
            try {
                if (board[row][col] == chess
                        && board[row+1][col-1] ==chess
                        && board[row+2][col-2] ==chess
                        && board[row+3][col-3] ==chess
                        && board[row+4][col-4] ==chess
                )
                    //胜负以分
                    return chess == 1 ? user1.getUserId() : user2.getUserId();
            }catch (ArrayIndexOutOfBoundsException e){
                continue;
            }
        }
        //胜负未分
        return 0;
    }
}