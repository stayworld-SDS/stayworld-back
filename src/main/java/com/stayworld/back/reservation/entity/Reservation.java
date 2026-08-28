package com.stayworld.back.reservation.entity;

import com.stayworld.back.guesthouse.entity.Guesthouse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    /** members 도메인 미완성이라 연관관계 대신 스칼라 FK 로 둔다. 응답에 회원 정보는 필요 없음. */
    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guesthouse_id", nullable = false)
    private Guesthouse guesthouse;

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
    private Reservation(Long userId, Guesthouse guesthouse, LocalDate startDate,
                        LocalDate endDate, int headcount, int cost) {
        this.userId = userId;
        this.guesthouse = guesthouse;
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
