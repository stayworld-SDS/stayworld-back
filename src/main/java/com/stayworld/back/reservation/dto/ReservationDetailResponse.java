package com.stayworld.back.reservation.dto;

import com.stayworld.back.guesthouse.entity.Guesthouse;
import com.stayworld.back.reservation.entity.Reservation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** GET /reservations/{reservationId} 상세. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDetailResponse {

    private Long reservationId;
    private Long guesthouseId;
    private String guesthouseName;
    private LocalDate startDate;
    private LocalDate endDate;
    private int headcount;
    private int cost;
    private String address;
    private int capacity;
    private boolean parking;
    private boolean wifi;
    private boolean breakfast;

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
