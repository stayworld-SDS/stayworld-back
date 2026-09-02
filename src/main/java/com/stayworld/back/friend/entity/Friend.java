package com.stayworld.back.friend.entity;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "friends",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "friend_id"}),
        // (user_id, friend_id) 유니크 인덱스가 user_id 선두 조회는 커버한다.
        // friend_id 단독(반대 방향 BFS 확장)만 별도 인덱스 필요.
        indexes = @Index(name = "idx_friend_friend_id", columnList = "friend_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "friend_id", nullable = false)
    private Long friendId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    private Friend(Long userId, Long friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
