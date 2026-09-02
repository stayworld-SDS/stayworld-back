package com.stayworld.back.wave.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 파도타기 추천 카드 1장.
 *
 * @param degreeFromMe 나로부터의 촌수. 2촌(친구의 친구)은 항상 채워지고, 그 외에는
 *                     {@code withDegree=true} 로 요청했을 때만 채워진다 (도달 불가/미계산이면 null).
 */
public record RecommendedUser(
        Long userId,
        String nickname,
        int visitorCount,
        long friendCount,
        LocalDateTime memberSince,
        Integer degreeFromMe,
        List<Reason> reasons
) {
}
