package com.stayworld.back.reservation.repository;

import com.stayworld.back.guesthouse.entity.Guesthouse;
import com.stayworld.back.reservation.entity.DailyOccupancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyOccupancyRepository extends JpaRepository<DailyOccupancy, Long> {

    @Query("""
        SELECT COALESCE(MAX(o.totalHeadcount), 0)
        FROM DailyOccupancy o
        WHERE o.guesthouse.id = :guesthouseId
            AND o.date >= :startDate
            AND o.date < :endDate
    """)
    int findMaxOccupancyByGuesthouseAndDateRange(
            long guesthouseId,
            LocalDate startDate,
            LocalDate endDate
    );



    @Query("""
        SELECT g
        FROM Guesthouse g
        WHERE g.capacity >= :headcount
            AND g.address LIKE CONCAT('%', :location, '%') ESCAPE '\\'
            AND NOT EXISTS (
            SELECT o
            FROM DailyOccupancy o
            WHERE o.guesthouse = g
            AND g.capacity - o.totalHeadcount < :headcount
            AND o.date >= :startDate
            AND o.date < :endDate
        )
        ORDER BY g.visitorCount DESC
    """)
    List<Guesthouse> findGuesthouseByLocationAndAvailability(
            String location,
            LocalDate startDate,
            LocalDate endDate,
            int headcount
    );

    @Modifying
    @Query(value = """
    INSERT INTO daily_occupancies (
        guesthouse_id,
        date,
        total_headcount
    )
    WITH RECURSIVE dates AS (
        SELECT CAST(:startDate AS DATE) AS stay_date

        UNION ALL

        SELECT DATE_ADD(stay_date, INTERVAL 1 DAY)
        FROM dates
        WHERE DATE_ADD(stay_date, INTERVAL 1 DAY) < :endDate
    )
    SELECT
        :guesthouseId,
        stay_date,
        :headcount
    FROM dates
    ON DUPLICATE KEY UPDATE
        total_headcount = daily_occupancies.total_headcount + :headcount
    """, nativeQuery = true)
    int increaseOccupancy(
            long guesthouseId,
            LocalDate startDate,
            LocalDate endDate,
            int headcount
    );

    @Modifying
    @Query("""
        UPDATE DailyOccupancy o
        SET o.totalHeadcount = o.totalHeadcount - :headcount
        WHERE o.guesthouse.id = :guesthouseId
            AND o.date >= :startDate
            AND o.date < :endDate
            AND o.totalHeadcount >= :headcount
    """)
    int decreaseOccupancy(
            long guesthouseId,
            LocalDate startDate,
            LocalDate endDate,
            int headcount
    );
}
