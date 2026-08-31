package com.stayworld.back.acorn.dto;

/** POST /games 응답. acorns = 참여비 차감 + 획득분 반영 후 남은 도토리 잔액. */
public record GamePlayResponse(
        int acorns
) {
}
