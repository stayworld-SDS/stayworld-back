package com.stayworld.back.acorn.repository;

import com.stayworld.back.acorn.entity.AcornHistory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link AcornHistoryRepository} 파생 쿼리를 실제 DB(H2)로 검증한다.
 */
@DataJpaTest
class AcornHistoryRepositoryTest {

    @Autowired
    AcornHistoryRepository acornHistoryRepository;

    private AcornHistory history(long userId, int amount, int balanceAfter, String reason) {
        return AcornHistory.builder()
                .userId(userId)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .reason(reason)
                .build();
    }

    @Test
    void findByUserId_해당_유저_내역만_pageable_정렬대로_반환한다() {
        acornHistoryRepository.save(history(1L, 50_000, 50_000, "SIGN_UP"));
        acornHistoryRepository.save(history(1L, -30_000, 20_000, "RESERVATION"));
        acornHistoryRepository.save(history(1L, 10_000, 30_000, "GAME_WIN"));
        acornHistoryRepository.save(history(2L, 50_000, 50_000, "SIGN_UP"));   // 다른 유저 — 섞이면 안 됨

        Page<AcornHistory> result = acornHistoryRepository.findByUserId(
                1L, PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id")));

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).extracting(AcornHistory::getReason)
                .containsExactly("GAME_WIN", "RESERVATION", "SIGN_UP");   // id 역순 = 저장 역순
        assertThat(result.getContent()).allMatch(h -> h.getUserId().equals(1L));
    }

    @Test
    void findByUserId_size만큼_페이지가_나뉘고_hasNext가_맞다() {
        for (int i = 0; i < 5; i++) {
            acornHistoryRepository.save(history(1L, 100, 100 * (i + 1), "GAME_ENTRY"));
        }

        Page<AcornHistory> first = acornHistoryRepository.findByUserId(
                1L, PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "id")));

        assertThat(first.getContent()).hasSize(2);
        assertThat(first.getTotalPages()).isEqualTo(3);   // 5개를 2개씩 -> 3페이지
        assertThat(first.hasNext()).isTrue();

        Page<AcornHistory> last = acornHistoryRepository.findByUserId(
                1L, PageRequest.of(2, 2, Sort.by(Sort.Direction.DESC, "id")));

        assertThat(last.getContent()).hasSize(1);
        assertThat(last.hasNext()).isFalse();
    }

    @Test
    void findByUserId_내역이_없으면_빈_페이지() {
        Page<AcornHistory> result = acornHistoryRepository.findByUserId(999L, PageRequest.of(0, 20));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}
