package com.stayworld.back.wave.repository;

import com.stayworld.back.wave.entity.WaveDailyLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface WaveDailyLogRepository extends JpaRepository<WaveDailyLog, Long> {

    /** 해당 유저가 그 날짜에 파도탄 횟수. 일일 제한 체크와 "첫 파도타기 여부" 판정에 같이 쓴다. */
    long countByUserIdAndWaveDate(Long userId, LocalDate waveDate);
}
