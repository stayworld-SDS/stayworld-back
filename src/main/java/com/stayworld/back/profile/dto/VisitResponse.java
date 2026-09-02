package com.stayworld.back.profile.dto;

/**
 * 미니홈피 방문 기록 결과.
 *
 * @param counted      이번 요청으로 투데이가 실제로 올랐는지 (본인 방문 / 오늘 재방문이면 false)
 * @param visitorCount 반영 후 홈피 주인의 누적 방문자 수
 */
public record VisitResponse(boolean counted, int visitorCount) {
}
