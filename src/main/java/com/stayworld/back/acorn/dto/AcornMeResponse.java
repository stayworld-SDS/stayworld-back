package com.stayworld.back.acorn.dto;

/** GET /acorns/me 응답. */
public record AcornMeResponse(
        int balance,
        int playCount,    // 오늘 게임 참여 횟수
        int dailyLimit     // 하루 참여 제한 (현재 10)
) {
}
