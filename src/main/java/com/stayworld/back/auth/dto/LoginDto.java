package com.stayworld.back.auth.dto;

import lombok.Data;

@Data
public class LoginDto {
    String email;
    String password;
}
