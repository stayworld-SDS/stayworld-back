package com.stayworld.back.acorn.repository;

import com.stayworld.back.acorn.entity.AcornHistory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

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
    void findByUserIdOrderByIdDesc_해당_유저_내역만_최신순으로_반환한다() {
        acornHistoryRepository.save(history(1L, 50_000, 50_000, "SIGN_UP"));
        acornHistoryRepository.save(history(1L, -30_000, 20_000, "RESERVATION"));
        acornHistoryRepository.save(history(1L, 10_000, 30_000, "GAME_WIN"));
        acornHistoryRepository.save(history(2L, 50_000, 50_000, "SIGN_UP"));   // 다른 유저 — 섞이면 안 됨

        List<AcornHistory> result = acornHistoryRepository.findByUserIdOrderByIdDesc(1L);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(AcornHistory::getReason)
                .containsExactly("GAME_WIN", "RESERVATION", "SIGN_UP");   // id 역순 = 저장 역순
        assertThat(result).allMatch(h -> h.getUserId().equals(1L));
        assertThat(result).allMatch(h -> h.getCreatedAt() != null);   // @PrePersist
    }

    @Test
    void findByUserIdOrderByIdDesc_내역이_없으면_빈_리스트() {
        List<AcornHistory> result = acornHistoryRepository.findByUserIdOrderByIdDesc(999L);

        assertThat(result).isEmpty();
    }
}
