package com.stayworld.back.wave.service;

import java.util.HashSet;
import java.util.Set;

/**
 * 추천 후보 1명에 대해 여러 소스(2촌 / 같은 게하 / 같은 지역 / 랜덤)에서 모은 신호를 누적한다.
 * {@link RecommendationService} 가 recall 단계에서 채우고 {@link RecommendationScorer} 가 읽는다.
 */
class CandidateAccumulator {

    final long userId;

    /** 함께 다녀온 게스트하우스 수. */
    int sharedGuesthouseCount;

    /** 겹치는 여행 지역 수. */
    int sharedRegionCount;

    /** 나와의 공통 일촌 id. size 가 곧 공통 일촌 수. */
    final Set<Long> mutualFriendIds = new HashSet<>();

    /** 신호 없이 랜덤 탐색으로만 뽑힌 후보인지. */
    boolean fromRandom;

    CandidateAccumulator(long userId) {
        this.userId = userId;
    }

    boolean isFriendOfFriend() {
        return !mutualFriendIds.isEmpty();
    }
}
