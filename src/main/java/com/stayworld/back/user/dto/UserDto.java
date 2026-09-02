package com.stayworld.back.user.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserDto {

    long id;
    String email;
    String nickname;
    String phoneNumber;
    int balance;
    LocalDateTime createdAt;
    int visitorCount;
    int profilePictureId;
}
