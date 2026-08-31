package com.stayworld.back.acorn.repository;

import com.stayworld.back.acorn.entity.AcornHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcornHistoryRepository extends JpaRepository<AcornHistory, Long> {

    /** 유저의 도토리 내역 페이지. 정렬은 호출자가 넘긴 {@code pageable} 을 따른다. */
    Page<AcornHistory> findByUserId(Long userId, Pageable pageable);
}
