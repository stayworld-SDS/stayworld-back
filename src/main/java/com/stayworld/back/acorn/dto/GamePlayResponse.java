package com.stayworld.back.acorn.dto;

/** POST /games 응답. acorns = 게임 후 남은 도토리 잔액, first/second/third = 슬롯 릴 값(0~9). */
public record GamePlayResponse(
        int acorns,
        int first,
        int second,
        int third
) {
}
