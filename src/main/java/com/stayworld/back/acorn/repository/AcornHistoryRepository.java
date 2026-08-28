package com.stayworld.back.acorn.repository;

import com.stayworld.back.acorn.entity.AcornHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcornHistoryRepository extends JpaRepository<AcornHistory, Long> {

    /** 유저의 전체 도토리 내역, 최신순. */
    List<AcornHistory> findByUserIdOrderByIdDesc(Long userId);
}
