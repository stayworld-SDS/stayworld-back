package com.stayworld.back.wave.dto;

/**
 * 파도타서 놀러간 결과.
 *
 * @param rewardedAcorns 이번 방문으로 받은 도토리 (그날 첫 파도타기가 아니면 0)
 * @param acornBalance   지급 반영 후 내 도토리 잔액
 * @param wavesToday     오늘 파도탄 횟수 (이번 것 포함)
 */
public record WaveResponse(
        Long targetUserId,
        int rewardedAcorns,
        int acornBalance,
        long wavesToday
) {
}
