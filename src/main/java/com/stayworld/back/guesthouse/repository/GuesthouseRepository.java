package com.stayworld.back.guesthouse.repository;

import com.stayworld.back.guesthouse.entity.Guesthouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface GuesthouseRepository extends JpaRepository<Guesthouse, Long> {

    /** 지역이 아직 안 채워진 게하 (수기 seed 로 들어온 행). 기동 시 백필 대상. */
    @Query("SELECT g FROM Guesthouse g WHERE g.region IS NULL OR g.region = ''")
    List<Guesthouse> findWithoutRegion();

    /** 주어진 게하들의 서로 다른 지역 목록 (내가 다녀온 지역 집합 계산용). */
    @Query("SELECT DISTINCT g.region FROM Guesthouse g WHERE g.id IN :ids")
    List<String> findDistinctRegionsByIdIn(Collection<Long> ids);
}
