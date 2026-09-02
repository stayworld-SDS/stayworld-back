package com.stayworld.back.friend.repository;

import com.stayworld.back.friend.entity.Friend;
import com.stayworld.back.global.dto.IdCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Friend> findByUserIdAndFriendId(Long userId, Long friendId);

    boolean existsByUserIdAndFriendId(Long userId, Long friendId);

    long countByUserId(Long userId);

    /** 촌수 BFS 한 레벨 확장용: 주어진 id들이 {@code user_id} 인 일촌 행. */
    List<Friend> findByUserIdIn(Collection<Long> userIds);

    /** 촌수 BFS 한 레벨 확장용: 주어진 id들이 {@code friend_id} 인 일촌 행 (반대 방향). */
    List<Friend> findByFriendIdIn(Collection<Long> friendIds);

    /** 추천 카드 하이드레이션용: 후보 유저별 일촌 수 (단방향 {@code user_id} 기준, {@code getFriendsOf} 와 동일 정의). */
    @Query("SELECT f.userId AS id, COUNT(f) AS count FROM Friend f WHERE f.userId IN :userIds GROUP BY f.userId")
    List<IdCount> countByUserIdIn(Collection<Long> userIds);
}
