package com.stayworld.back.profile.dto;

import java.time.LocalDateTime;

/** 미니홈피에 남은 발자국 한 개 (방문자별 가장 최근 방문). */
public record FootprintDto(Long visitorId, String nickname, LocalDateTime visitedAt) {
}
