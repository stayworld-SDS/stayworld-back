package com.stayworld.back.acorn.repository;

import com.stayworld.back.acorn.entity.AcornDailyPlay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface AcornDailyPlayRepository extends JpaRepository<AcornDailyPlay, Long> {

    boolean existsByUserIdAndPlayDate(Long userId, LocalDate playDate);
}
