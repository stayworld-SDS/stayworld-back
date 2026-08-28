package com.stayworld.back.reservation.controller;

import com.stayworld.back.global.response.ApiResponse;
import com.stayworld.back.reservation.dto.ReservationCreateRequest;
import com.stayworld.back.reservation.dto.ReservationDetailResponse;
import com.stayworld.back.reservation.dto.ReservationSummaryResponse;
import com.stayworld.back.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

/**
 * 현재 로그인 유저 식별: 로그인 도메인이 세션에 {@code "userId"} (Long) 를 넣어준다고 가정.
 * 세션에 없으면 지금은 500 으로 떨어짐 → 로그인 도메인 붙을 때 401 처리(인터셉터/ArgumentResolver)로 교체.
 */
@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // 유저 예약목록 조회
    @GetMapping("/me")
    public ApiResponse<List<ReservationSummaryResponse>> getMyReservations(
            @SessionAttribute("userId") Long userId) {
        return ApiResponse.success(reservationService.getMyReservations(userId));
    }

    // 예약 상세 조회
    @GetMapping("/{reservationId}")
    public ApiResponse<ReservationDetailResponse> getReservation(
            @PathVariable Long reservationId,
            @SessionAttribute("userId") Long userId) {
        return ApiResponse.success(reservationService.getReservation(reservationId, userId));
    }

    // 예약
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> create(
            @SessionAttribute("userId") Long userId,
            @Valid @RequestBody ReservationCreateRequest request) {
        Long reservationId = reservationService.create(userId, request);
        return ApiResponse.success("예약이 완료되었습니다.", reservationId);
    }

    // 예약 삭제
    @DeleteMapping("/{reservationId}")
    public ApiResponse<Void> delete(
            @PathVariable Long reservationId,
            @SessionAttribute("userId") Long userId) {
        reservationService.delete(reservationId, userId);
        return ApiResponse.success(null);
    }
}
