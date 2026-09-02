package com.stayworld.back.wave.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 파도타서 놀러간 기록 1건. {@code (user_id, wave_date)} 로 세어 하루 횟수 제한과
 * "그날 첫 파도타기(=보상 지급)" 판정에 쓴다. {@code target_user_id} 는 나중에
 * "어떤 추천이 실제 방문으로 이어졌나" 튜닝용으로 남겨둔다.
 *
 * <p>TODO(동시성): {@code AcornDailyPlay} 와 동일하게 count 후 insert 라
 * 동시 요청이 정확히 몰리면 하루 제한을 살짝 넘길 수 있다 ({@code TODO.md} 참고).
 */
@Entity
@Table(name = "wave_daily_log", indexes = @Index(name = "idx_wave_user_date", columnList = "user_id, wave_date"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WaveDailyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wave_date", nullable = false)
    private LocalDate waveDate;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    /** 이 방문으로 그날 첫 파도타기 보상을 지급했는지. */
    @Column(nullable = false)
    private boolean rewarded;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public WaveDailyLog(Long userId, LocalDate waveDate, Long targetUserId, boolean rewarded) {
        this.userId = userId;
        this.waveDate = waveDate;
        this.targetUserId = targetUserId;
        this.rewarded = rewarded;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
