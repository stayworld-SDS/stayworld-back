package com.stayworld.back.acorn.controller;

import com.stayworld.back.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AcornController {

    // 게임 참여 (난수 3개 생성, 도토리 차감/증가)
    @PostMapping("/games")
    public ApiResponse<?> playGame() {
        return null;
    }

    // 도토리 사용/습득 내역 조회
    @GetMapping("/acorns/history")
    public ApiResponse<?> getHistory() {
        return null;
    }

    // 내 현재 도토리 잔액, 오늘 게임 참여 여부
    @GetMapping("/acorns/me")
    public ApiResponse<?> getMyAcorn() {
        return null;
    }
}
