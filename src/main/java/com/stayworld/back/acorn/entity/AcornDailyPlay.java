package com.stayworld.back.acorn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 하루 1회 게임 참여 제한용 기록. {@code (user_id, play_date)} 유니크 제약이 있어
 * 동시 요청으로 두 번 insert 되면 두 번째가 무결성 위반으로 막힌다.
 */
@Entity
@Table(
        name = "acorn_daily_play",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_acorn_daily_play_user_date",
                columnNames = {"user_id", "play_date"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcornDailyPlay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "play_date", nullable = false)
    private LocalDate playDate;

    public AcornDailyPlay(Long userId, LocalDate playDate) {
        this.userId = userId;
        this.playDate = playDate;
    }
}
