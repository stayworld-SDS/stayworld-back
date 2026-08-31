package com.stayworld.back.reservation.dto;

import com.stayworld.back.reservation.entity.Reservation;
import com.stayworld.back.reservation.support.GuesthouseInfo;

import java.time.LocalDate;

/** GET /reservations/{reservationId} 상세. */
public record ReservationDetailResponse(
        Long reservationId,
        Long guesthouseId,
        String guesthouseName,
        LocalDate startDate,
        LocalDate endDate,
        int headcount,
        int cost,
        String address,
        int capacity,
        boolean parking,
        boolean wifi,
        boolean breakfast
) {
    public static ReservationDetailResponse from(Reservation r, GuesthouseInfo g) {
        return new ReservationDetailResponse(
                r.getId(),
                g.id(),
                g.name(),
                r.getStartDate(),
                r.getEndDate(),
                r.getHeadcount(),
                r.getCost(),
                g.address(),
                g.capacity(),
                g.parking(),
                g.wifi(),
                g.breakfast()
        );
    }
}
