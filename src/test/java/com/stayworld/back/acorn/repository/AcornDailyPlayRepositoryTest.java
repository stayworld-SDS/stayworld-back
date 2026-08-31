package com.stayworld.back.acorn.repository;

import com.stayworld.back.acorn.entity.AcornDailyPlay;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link AcornDailyPlayRepository} 파생 쿼리 + {@code acorn_daily_play} 의
 * {@code (user_id, play_date)} 유니크 제약을 실제 DB(H2)로 검증한다.
 */
@DataJpaTest
class AcornDailyPlayRepositoryTest {

    @Autowired
    AcornDailyPlayRepository acornDailyPlayRepository;

    private static final LocalDate TODAY = LocalDate.now();

    @Test
    void existsByUserIdAndPlayDate_해당_유저가_그날_참여했으면_true() {
        acornDailyPlayRepository.save(new AcornDailyPlay(1L, TODAY));

        assertThat(acornDailyPlayRepository.existsByUserIdAndPlayDate(1L, TODAY)).isTrue();
    }

    @Test
    void existsByUserIdAndPlayDate_다른_날짜나_다른_유저면_false() {
        acornDailyPlayRepository.save(new AcornDailyPlay(1L, TODAY));

        assertThat(acornDailyPlayRepository.existsByUserIdAndPlayDate(1L, TODAY.plusDays(1))).isFalse();
        assertThat(acornDailyPlayRepository.existsByUserIdAndPlayDate(2L, TODAY)).isFalse();
    }

    @Test
    void 같은_유저가_같은_날짜에_두번_저장하면_유니크_제약_위반() {
        acornDailyPlayRepository.saveAndFlush(new AcornDailyPlay(1L, TODAY));

        assertThatThrownBy(() -> acornDailyPlayRepository.saveAndFlush(new AcornDailyPlay(1L, TODAY)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 같은_유저라도_날짜가_다르면_따로_저장된다() {
        acornDailyPlayRepository.saveAndFlush(new AcornDailyPlay(1L, TODAY));
        acornDailyPlayRepository.saveAndFlush(new AcornDailyPlay(1L, TODAY.plusDays(1)));

        assertThat(acornDailyPlayRepository.existsByUserIdAndPlayDate(1L, TODAY)).isTrue();
        assertThat(acornDailyPlayRepository.existsByUserIdAndPlayDate(1L, TODAY.plusDays(1))).isTrue();
    }
}
