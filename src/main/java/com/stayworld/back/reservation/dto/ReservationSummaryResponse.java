package com.stayworld.back.reservation.dto;

import com.stayworld.back.reservation.entity.Reservation;
import com.stayworld.back.reservation.support.GuesthouseInfo;

import java.time.LocalDate;

/** GET /reservations/me 목록 항목. */
public record ReservationSummaryResponse(
        Long reservationId,
        Long guesthouseId,
        String guesthouseName,
        LocalDate startDate,
        LocalDate endDate,
        int headcount
) {
    public static ReservationSummaryResponse from(Reservation r, GuesthouseInfo g) {
        return new ReservationSummaryResponse(
                r.getId(),
                g.id(),
                g.name(),
                r.getStartDate(),
                r.getEndDate(),
                r.getHeadcount()
        );
    }
}
