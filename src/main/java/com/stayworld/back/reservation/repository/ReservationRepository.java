package com.stayworld.back.reservation.repository;

import com.stayworld.back.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /** 유저의 '유효한'(체크아웃이 오늘 이후) 예약 목록. */
    List<Reservation> findByUserIdAndEndDateGreaterThanEqualOrderByStartDateAsc(Long userId, LocalDate today);

    /** 유저가 다녀온(체크아웃이 오늘 이전) 예약 목록, 최근 순. */
    List<Reservation> findByUserIdAndEndDateLessThanOrderByStartDateDesc(Long userId, LocalDate today);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.userId = :userId
            AND r.guesthouse.id = :guesthouseId
            AND r.endDate <= :today
            AND NOT EXISTS (
                SELECT gb
                FROM Guestbook gb
                WHERE gb.reservation = r
            )
        ORDER BY r.endDate DESC, r.id DESC
    """)
    List<Reservation> findLatestGuestbookEligibleReservation(
            Long userId,
            Long guesthouseId,
            LocalDate today,
            Pageable pageable
    );
}
