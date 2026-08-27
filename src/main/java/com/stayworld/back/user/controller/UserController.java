package com.stayworld.back.user.controller;

import com.stayworld.back.global.response.ApiResponse;
import com.stayworld.back.user.dto.DeleteDto;
import com.stayworld.back.user.dto.LoginDto;
import com.stayworld.back.user.dto.UserDto;
import org.springframework.web.bind.annotation.*;

@RestController
class UserController {
    @PostMapping("/users")
    public ApiResponse createUser() {
        return ApiResponse.success("유저 생성에 성공하였습니다.", null);
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<UserDto> getUserDetailsById(@PathVariable("userId") int userId) {
        return ApiResponse.success(null);
    }

    @GetMapping("/users/me")
    public ApiResponse getUserDetailsBySession() {
        return ApiResponse.success(null);
    }

    @PostMapping("/users/check-email/{email}")
    public ApiResponse checkEmailOccupancy(@PathVariable("email") String email) {
        return ApiResponse.success(null);
    }

    @PatchMapping("/users/me")
    public ApiResponse modifyUserDetails() {
        return ApiResponse.success(null);
    }

    @DeleteMapping("/users/me")
    public ApiResponse deleteUser(DeleteDto dto) {
        return ApiResponse.success(null);
    }

    @PostMapping("/auth/login")
    public ApiResponse login(LoginDto dto) {
        return ApiResponse.success(null);
    }

    @PostMapping("/auth/logout")
    public ApiResponse logout() {
        return ApiResponse.success(null);
    }
}
