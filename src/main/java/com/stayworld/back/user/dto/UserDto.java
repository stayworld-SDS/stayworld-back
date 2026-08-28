package com.stayworld.back.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {
    long id;
    String password;
    String email;
    String nickname;
    String phoneNumber;
    int balance;
    LocalDateTime createdAt;
    int visitorCount;
}
