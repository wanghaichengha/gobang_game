package com.example.gobang_game.game;

import com.sun.org.apache.bcel.internal.generic.NEW;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomManger {
     //roomId和房间room的管理关系
     private ConcurrentHashMap<String,Room> rooms = new ConcurrentHashMap<>();
     //玩家和房间的管理关系
     private ConcurrentHashMap<Integer,String> userIdToRoomId = new ConcurrentHashMap<>();

     //添加方法
     public void add(Room room,int userId1,int userId2){
         rooms.put(room.getRoomId(),room);
         userIdToRoomId.put(userId1, room.getRoomId());
         userIdToRoomId.put(userId2, room.getRoomId());
     }
     //删除方法
    public void remove(String roomId,int userId1, int userId2){
         rooms.remove(roomId);
         userIdToRoomId.remove(userId1);
         userIdToRoomId.remove(userId2);
    }
    //根据房间id查找房间信息
    public Room getRoomByRoomId(String roomId){
         return rooms.get(roomId);
    }
    //根据用户id查找用户所在房间
    public Room getRoomByUserId(int userId){
         String roomId = userIdToRoomId.get(userId);
         if (roomId == null){
             return null;
         }
         return rooms.get(roomId);
    }

}
