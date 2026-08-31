package com.stayworld.back.user.controller;

import com.stayworld.back.global.exception.UnauthorizedException;
import com.stayworld.back.global.response.ApiResponse;
import com.stayworld.back.user.dto.CreateDto;
import com.stayworld.back.user.dto.DeleteDto;
import com.stayworld.back.user.dto.ModifyDto;
import com.stayworld.back.user.dto.UserDto;
import com.stayworld.back.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    public static final String SESSION_MEMBER_ID = "MEMBER_ID";

    @Autowired
    private final UserService userService;

    @PostMapping("/users")
    public ApiResponse<UserDto> createUser(@Valid @RequestBody CreateDto dto) {
        return ApiResponse.success("유저 생성에 성공하였습니다.", userService.createUser(dto));
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<UserDto> getUserDetailsById(@PathVariable("userId") long userId) {
        return ApiResponse.success(userService.getUserById(userId));
    }

    @GetMapping("/users/me")
    public ApiResponse<UserDto> getUserDetailsBySession(HttpSession session) {
        return ApiResponse.success(userService.getUserById(getLoginMemberId(session)));
    }

    @GetMapping("/users/check-email/{email}")
    public ApiResponse<Boolean> checkEmailOccupancy(
            @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}$")
            @PathVariable("email") String email) {
        return ApiResponse.success(userService.checkEmailOccupancy(email));
    }

    @PatchMapping("/users/me")
    public ApiResponse<UserDto> modifyUserDetails(@RequestBody ModifyDto dto, HttpSession session) {
        return ApiResponse.success(userService.modifyUserDetails(getLoginMemberId(session), dto));
    }

    @DeleteMapping("/users/me")
    public ApiResponse<Void> deleteUser(@RequestBody DeleteDto dto, HttpSession session) {
        userService.deleteUser(getLoginMemberId(session), dto);
        session.invalidate();
        return ApiResponse.success("회원 탈퇴가 완료되었습니다.", null);
    }


    private long getLoginMemberId(HttpSession session) {
        Object memberId = session.getAttribute(SESSION_MEMBER_ID);
        if (memberId == null) {
            throw new UnauthorizedException();
        }
        return (long) memberId;
    }
}
