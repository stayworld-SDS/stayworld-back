package com.stayworld.back.reservation.entity;

import com.stayworld.back.guesthouse.entity.Guesthouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations", indexes = {
        @Index(name = "idx_reservation_user_end", columnList = "user_id, end_date"),
        @Index(name = "idx_reservation_guesthouse_end", columnList = "guesthouse_id, end_date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** members 도메인 미완성 + 응답에 회원 정보 불필요 → 스칼라 FK. */
    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
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
