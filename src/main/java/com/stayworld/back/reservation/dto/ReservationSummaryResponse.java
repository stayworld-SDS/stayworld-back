package com.stayworld.back.reservation.dto;

import com.stayworld.back.guesthouse.entity.Guesthouse;
import com.stayworld.back.reservation.entity.Reservation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** GET /reservations/me 목록 항목. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationSummaryResponse {

    private Long reservationId;
    private Long guesthouseId;
    private String guesthouseName;
    private LocalDate startDate;
    private LocalDate endDate;
    private int headcount;

    public static ReservationSummaryResponse from(Reservation r, Guesthouse g) {
        return new ReservationSummaryResponse(
                r.getId(),
                g.getId(),
                g.getName(),
                r.getStartDate(),
                r.getEndDate(),
                r.getHeadcount()
        );
    }
}
