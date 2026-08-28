package com.stayworld.back.acorn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 도토리 증감 원장(공용). 게임뿐 아니라 다른 도메인의 도토리 사용/습득도 여기에 1행씩 쌓인다.
 * 모든 기록은 {@code com.stayworld.back.acorn.service.AcornLedger} 를 통해 생성된다.
 */
@Entity
@Table(name = "acorn_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcornHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    /** 부호 있는 증감량. 잔액 하한(0) clamp 가 적용된 '실제 반영된' 값. */
    @Column(nullable = false)
    private int amount;

    /** 이 거래 직후 잔액 (손 DDL 의 remaining_balance). */
    @Column(name = "balance_after", nullable = false)
    private int balanceAfter;

    @Column(length = 50, nullable = false)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    private AcornHistory(Long userId, int amount, int balanceAfter, String reason) {
        this.userId = userId;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.reason = reason;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
