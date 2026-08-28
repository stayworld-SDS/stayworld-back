package com.stayworld.back.reservation.entity;

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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** members 도메인 미완성 + 응답에 회원 정보 불필요 → 스칼라 FK. */
    @Column(nullable = false)
    private Long userId;

    /** guesthouse 도메인을 건드리지 않으려고 연관관계 대신 스칼라 FK. 숙소 정보는 GuesthouseReader 로 조회. */
    @Column(name = "guesthouse_id", nullable = false)
    private Long guesthouseId;

    @Column(nullable = false)
    private LocalDate startDate;   // 체크인

    @Column(nullable = false)
    private LocalDate endDate;     // 체크아웃

    @Column(nullable = false)
    private int headcount;

    @Column(nullable = false)
    private int cost;              // 서버에서 계산 (1박 요금 × 박 수)

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    private Reservation(Long userId, Long guesthouseId, LocalDate startDate,
                        LocalDate endDate, int headcount, int cost) {
        this.userId = userId;
        this.guesthouseId = guesthouseId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.headcount = headcount;
        this.cost = cost;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public boolean isOwnedBy(Long memberId) {
        return this.userId.equals(memberId);
    }
}
