package com.stayworld.back.reservation.repository;

import com.stayworld.back.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /** 유저의 '유효한'(체크아웃이 오늘 이후) 예약 목록. guesthouse 는 함께 로딩해 N+1 방지. */
    @EntityGraph(attributePaths = "guesthouse")
    List<Reservation> findByUserIdAndEndDateGreaterThanEqualOrderByStartDateAsc(Long userId, LocalDate today);

    @Override
    @EntityGraph(attributePaths = "guesthouse")
    Optional<Reservation> findById(Long id);

    /**
     * 같은 숙소에 대해 기간이 겹치는 예약이 있는지.
     * 겹침 조건: 기존.startDate < 요청.endDate AND 기존.endDate > 요청.startDate
     */
    boolean existsByGuesthouse_IdAndStartDateLessThanAndEndDateGreaterThan(
            Long guesthouseId, LocalDate requestedEnd, LocalDate requestedStart);
}
