package com.stayworld.back.friend.controller;

import com.stayworld.back.friend.dto.FriendAddRequest;
import com.stayworld.back.friend.dto.FriendDto;
import com.stayworld.back.friend.service.FriendService;
import com.stayworld.back.global.auth.LoginMember;
import com.stayworld.back.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping
    public ApiResponse<List<FriendDto>> getMyFriends(@LoginMember Long userId) {
        return ApiResponse.success(friendService.getMyFriends(userId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FriendDto> addFriend(
            @LoginMember Long userId,
            @Valid @RequestBody FriendAddRequest request) {
        return ApiResponse.success("일촌을 추가했습니다.", friendService.addFriend(userId, request.getTargetUserId()));
    }

    @DeleteMapping("/{targetUserId}")
    public ApiResponse<Void> removeFriend(
            @LoginMember Long userId,
            @PathVariable Long targetUserId) {
        friendService.removeFriend(userId, targetUserId);
        return ApiResponse.success("일촌을 삭제했습니다.", null);
    }
}
