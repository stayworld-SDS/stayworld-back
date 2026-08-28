package com.stayworld.back.guesthouse.repository;

import com.stayworld.back.guesthouse.entity.Guesthouse;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ⚠️ 껍데기 - guesthouse 도메인 담당자 작업 중이라고 가정.
 * reservation 도메인이 숙소 조회를 위해 사용하는 최소 정의만 둔다.
 */
public interface GuesthouseRepository extends JpaRepository<Guesthouse, Long> {
}
