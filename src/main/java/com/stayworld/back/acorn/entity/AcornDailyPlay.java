package com.stayworld.back.acorn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 게임 참여 1회당 기록 하나. 하루 참여 횟수 제한(현재 {@link com.stayworld.back.acorn.service.AcornService}
 * 에서 10회)은 {@code (user_id, play_date)} 로 이 테이블을 세어(count) 체크한다.
 *
 * <p>TODO(동시성): 체크 후 insert 라 동시 요청이 정확히 몰리면 하루 제한을 살짝 넘길 수 있다.
 * (다른 동시성 이슈와 마찬가지로 이번 범위 밖 — {@code TODO.md} 참고)
 */
@Entity
@Table(name = "acorn_daily_play")
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
