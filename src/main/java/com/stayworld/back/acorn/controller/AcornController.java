package com.stayworld.back.acorn.controller;

import com.stayworld.back.acorn.dto.AcornHistoryResponse;
import com.stayworld.back.acorn.dto.AcornMeResponse;
import com.stayworld.back.acorn.dto.GamePlayResponse;
import com.stayworld.back.acorn.service.AcornService;
import com.stayworld.back.global.auth.LoginMember;
import com.stayworld.back.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AcornController {

    private final AcornService acornService;

    // 게임 참여 (777 슬롯, 난수 3개 생성, 도토리 차감/증가, 하루 1회)
    @PostMapping("/games")
    public ApiResponse<GamePlayResponse> playGame(@LoginMember Long userId) {
        return ApiResponse.success(acornService.play(userId));
    }

    // 도토리 사용/습득 내역 조회
    @GetMapping("/acorns/history")
    public ApiResponse<AcornHistoryResponse> getHistory(@LoginMember Long userId) {
        return ApiResponse.success(acornService.history(userId));
    }

    // 내 현재 도토리 잔액, 오늘 게임 참여 여부
    @GetMapping("/acorns/me")
    public ApiResponse<AcornMeResponse> getMyAcorn(@LoginMember Long userId) {
        return ApiResponse.success(acornService.me(userId));
    }
}
