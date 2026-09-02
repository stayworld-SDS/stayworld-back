package com.stayworld.back.friend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendDto {
    Long userId;
    String nickname;
    LocalDateTime since;
}
