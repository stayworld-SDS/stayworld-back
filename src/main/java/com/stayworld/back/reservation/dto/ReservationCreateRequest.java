package com.stayworld.back.reservation.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * POST /reservations 요청 본문. userId 는 세션에서, cost/createdAt 은 서버에서 채운다.
 * 단일 필드 제약은 여기서, 교차 검증(체크인&lt;체크아웃)·업무 규칙(정원 초과, 기간 겹침)은 서비스에서.
 */
public record ReservationCreateRequest(

        @NotNull(message = "숙소 ID는 필수입니다.")
        Long guesthouseId,

        @NotNull(message = "체크인 날짜는 필수입니다.")
        @FutureOrPresent(message = "지난 날짜는 예약할 수 없습니다.")
        LocalDate startDate,

        @NotNull(message = "체크아웃 날짜는 필수입니다.")
        LocalDate endDate,

        @Min(value = 1, message = "인원수는 1명 이상이어야 합니다.")
        int headcount
) {
}
