package com.stayworld.back.user.dto;

import lombok.Data;

@Data
public class CreateDto {
    String password;
    String email;
    String nickname;
    String phoneNumber;
}
