package com.stayworld.back.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 미니홈피 방문(발자국) 1건. {@code (owner_id, visitor_id, visit_date)} 유니크로
 * "같은 사람이 같은 날" 다시 와도 투데이 카운트는 한 번만 오르게 한다.
 * 파도타기 / 검색 / 직접 링크 등 모든 진입 경로가 이 이벤트로 모인다.
 */
@Entity
@Table(
        name = "profile_visits",
        uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "visitor_id", "visit_date"}),
        indexes = @Index(name = "idx_profile_visit_owner", columnList = "owner_id, id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "visitor_id", nullable = false)
    private Long visitorId;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ProfileVisit(Long ownerId, Long visitorId, LocalDate visitDate) {
        this.ownerId = ownerId;
        this.visitorId = visitorId;
        this.visitDate = visitDate;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
