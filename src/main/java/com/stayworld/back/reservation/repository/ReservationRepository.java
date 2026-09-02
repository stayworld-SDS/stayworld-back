package com.stayworld.back.reservation.repository;

import com.stayworld.back.global.dto.IdCount;
import com.stayworld.back.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /** 유저의 '유효한'(체크아웃이 오늘 이후) 예약 목록. */
    List<Reservation> findByUserIdAndEndDateGreaterThanEqualOrderByStartDateAsc(Long userId, LocalDate today);

    /** 유저가 다녀온(체크아웃이 오늘 이전) 예약 목록, 최근 순. */
    List<Reservation> findByUserIdAndEndDateLessThanOrderByStartDateDesc(Long userId, LocalDate today);

    @Query("SELECT COUNT(DISTINCT r.guesthouse.id) FROM Reservation r WHERE r.userId = :userId AND r.endDate < :today")
    long countDistinctVisitedGuesthouses(Long userId, LocalDate today);

    /** 유저가 다녀온(체크아웃 지난) 서로 다른 게하 id. 파도타기 추천의 내 발자취. */
    @Query("SELECT DISTINCT r.guesthouse.id FROM Reservation r WHERE r.userId = :userId AND r.endDate < :today")
    List<Long> findDistinctVisitedGuesthouseIds(Long userId, LocalDate today);

    /** 내가 다녀온 게하들에 함께 다녀온 유저 → (유저 id, 공유 게하 수). */
    @Query("""
        SELECT r.userId AS id, COUNT(DISTINCT r.guesthouse.id) AS count
        FROM Reservation r
        WHERE r.guesthouse.id IN :guesthouseIds
            AND r.endDate < :today
            AND r.userId <> :excludeUserId
        GROUP BY r.userId
    """)
    List<IdCount> findCoVisitors(Collection<Long> guesthouseIds, LocalDate today, Long excludeUserId);

    /** 내가 다녀온 지역의 게하에 다녀온 유저 → (유저 id, 공유 지역 수). */
    @Query("""
        SELECT r.userId AS id, COUNT(DISTINCT r.guesthouse.region) AS count
        FROM Reservation r
        WHERE r.guesthouse.region IN :regions
            AND r.endDate < :today
            AND r.userId <> :excludeUserId
        GROUP BY r.userId
    """)
    List<IdCount> findCoRegionVisitors(Collection<String> regions, LocalDate today, Long excludeUserId);
}
