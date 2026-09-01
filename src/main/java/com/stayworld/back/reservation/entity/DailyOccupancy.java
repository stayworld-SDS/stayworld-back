package com.stayworld.back.reservation.entity;

import com.stayworld.back.guesthouse.entity.Guesthouse;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

@Entity
@Table(
        name = "daily_occupancies",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_daily_occupancy_guesthouse_date",
                columnNames = {"guesthouse_id", "date"}
        ),
        check = @CheckConstraint(
                name = "ck_daily_occupancy_non_negative",
                constraint = "total_headcount >= 0"
        )
)
@Getter
public class DailyOccupancy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Column(nullable = false)
    LocalDate date;
    @Column(nullable = false)
    int totalHeadcount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guesthouse_id", nullable = false)
    Guesthouse guesthouse;
}
