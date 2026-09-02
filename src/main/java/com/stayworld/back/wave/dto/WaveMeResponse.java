package com.stayworld.back.wave.dto;

/**
 * 오늘 내 파도타기 현황.
 *
 * @param rewardClaimed 오늘 첫 파도타기 보상을 이미 받았는지 (= 오늘 1회 이상 파도탐)
 */
public record WaveMeResponse(
        long wavesToday,
        boolean rewardClaimed,
        int dailyLimit
) {
}
