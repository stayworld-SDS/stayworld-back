package com.stayworld.back.auth.controller;

import com.stayworld.back.auth.dto.LoginDto;
import com.stayworld.back.auth.service.AuthService;
import com.stayworld.back.global.response.ApiResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.stayworld.back.user.controller.UserController.SESSION_MEMBER_ID;

@RestController
@RequiredArgsConstructor
public class AuthController {
    AuthService authService;

    @PostMapping("/auth/login")
    public ApiResponse<Void> login(@RequestBody LoginDto dto, HttpSession session) {
        long memberId = authService.login(dto);
        session.setAttribute(SESSION_MEMBER_ID, memberId);
        return ApiResponse.success("로그인에 성공하였습니다.", null);
    }

    @PostMapping("/auth/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        session.invalidate();
        return ApiResponse.success("로그아웃에 성공하였습니다.", null);
    }
}
