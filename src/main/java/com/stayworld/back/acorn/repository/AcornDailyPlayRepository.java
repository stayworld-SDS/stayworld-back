package com.stayworld.back.acorn.repository;

import com.stayworld.back.acorn.entity.AcornDailyPlay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface AcornDailyPlayRepository extends JpaRepository<AcornDailyPlay, Long> {

    /** 해당 유저가 그 날짜에 참여한 횟수. 일일 참여 제한 체크와 "몇 회 참여했는지" 조회에 같이 쓴다. */
    long countByUserIdAndPlayDate(Long userId, LocalDate playDate);
}
