package com.stayworld.back.user.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserDto {
    int id;
    String password;
    String email;
    String phoneNumber;
    int balance;
    Date createdAt;
    int visitorCount;
}
