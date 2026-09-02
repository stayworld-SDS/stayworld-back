package com.stayworld.back.user.repository;

import com.stayworld.back.user.entity.ProfileMusic;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileMusicRepository extends JpaRepository<ProfileMusic, Long> {

    @EntityGraph(attributePaths = "music")
    List<ProfileMusic> findByUserIdOrderByCreatedAtDesc(Long userId);
}
