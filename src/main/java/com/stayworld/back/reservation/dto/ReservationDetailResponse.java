package com.stayworld.back.reservation.dto;

import com.stayworld.back.guesthouse.entity.Guesthouse;
import com.stayworld.back.reservation.entity.Reservation;

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
    public static ReservationDetailResponse from(Reservation r, Guesthouse g) {
        return new ReservationDetailResponse(
                r.getId(),
                g.getId(),
                g.getName(),
                r.getStartDate(),
                r.getEndDate(),
                r.getHeadcount(),
                r.getCost(),
                g.getAddress(),
                g.getCapacity(),
                g.isParkingProvided(),
                g.isWifiProvided(),
                g.isBreakfastProvided()
        );
    }
}
