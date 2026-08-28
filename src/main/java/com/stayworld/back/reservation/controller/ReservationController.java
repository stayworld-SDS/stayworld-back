package com.stayworld.back.reservation.controller;

import com.stayworld.back.global.response.ApiResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    // 유저 예약목록 조회
    @GetMapping("/me")
    public ApiResponse<?> getMyReservations() {
        return null;
    }

    // 예약 상세 조회
    @GetMapping("/{reservationId}")
    public ApiResponse<?> getReservation(@PathVariable Long reservationId) {
        return null;
    }

    // 예약
    @PostMapping
    public ApiResponse<?> create() {
        return null;
    }

    // 예약 삭제
    @DeleteMapping
    public ApiResponse<?> delete() {
        return null;
    }
}
