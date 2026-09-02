package com.stayworld.back.wave.service;

import com.stayworld.back.wave.dto.Reason;

import java.util.List;

/** {@link RecommendationScorer} 의 출력. 하이드레이션(닉네임 등) 전 상태. */
record ScoredCandidate(long userId, double score, List<Reason> reasons) {
}
