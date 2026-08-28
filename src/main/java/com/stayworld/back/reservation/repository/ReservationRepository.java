package com.stayworld.back.reservation.repository;

import com.stayworld.back.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /** 유저의 '유효한'(체크아웃이 오늘 이후) 예약 목록. */
    List<Reservation> findByUserIdAndEndDateGreaterThanEqualOrderByStartDateAsc(Long userId, LocalDate today);

    /**
     * 같은 숙소에 대해 기간이 겹치는 예약이 있는지.
     * 겹침 조건: 기존.startDate < 요청.endDate AND 기존.endDate > 요청.startDate
     */
    boolean existsByGuesthouseIdAndStartDateLessThanAndEndDateGreaterThan(
            Long guesthouseId, LocalDate requestedEnd, LocalDate requestedStart);
}
