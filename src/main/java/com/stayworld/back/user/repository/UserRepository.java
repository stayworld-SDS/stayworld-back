package com.stayworld.back.user.repository;

import com.stayworld.back.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByNicknameContaining(String keyword);

    /** 파도타기 랜덤 채움용. 호출자가 {@code PageRequest.of(randomOffset, 1)} 로 한 명씩 뽑는다. */
    @Query("SELECT u FROM User u WHERE u.id <> :excludeId ORDER BY u.id")
    List<User> findRandomPool(Long excludeId, Pageable pageable);
}
