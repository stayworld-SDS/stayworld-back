package com.stayworld.back.acorn.repository;

import com.stayworld.back.acorn.entity.AcornDailyPlay;
import com.stayworld.back.acorn.service.AcornService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link AcornDailyPlayRepository} 파생 쿼리를 실제 DB(H2)로 검증한다.
 * (하루 참여 제한이 10회로 늘어나며 unique 제약은 없어지고, count 기반 체크로 바뀌었다.)
 */
@DataJpaTest
class AcornDailyPlayRepositoryTest {

    @Autowired
    AcornDailyPlayRepository acornDailyPlayRepository;

    private static final LocalDate TODAY = LocalDate.now();

    @Test
    void countByUserIdAndPlayDate_참여한_횟수만큼_반환한다() {
        acornDailyPlayRepository.save(new AcornDailyPlay(1L, TODAY));
        acornDailyPlayRepository.save(new AcornDailyPlay(1L, TODAY));
        acornDailyPlayRepository.save(new AcornDailyPlay(1L, TODAY));

        assertThat(acornDailyPlayRepository.countByUserIdAndPlayDate(1L, TODAY)).isEqualTo(3);
    }

    @Test
    void countByUserIdAndPlayDate_참여기록이_없으면_0() {
        assertThat(acornDailyPlayRepository.countByUserIdAndPlayDate(1L, TODAY)).isZero();
    }

    @Test
    void countByUserIdAndPlayDate_다른_날짜나_다른_유저는_섞이지_않는다() {
        acornDailyPlayRepository.save(new AcornDailyPlay(1L, TODAY));
        acornDailyPlayRepository.save(new AcornDailyPlay(1L, TODAY.plusDays(1)));
        acornDailyPlayRepository.save(new AcornDailyPlay(2L, TODAY));

        assertThat(acornDailyPlayRepository.countByUserIdAndPlayDate(1L, TODAY)).isEqualTo(1);
    }

    @Test
    void 같은_유저가_같은_날짜에_여러번_참여해도_전부_기록된다() {
        for (int i = 0; i < AcornService.DAILY_PLAY_LIMIT; i++) {
            acornDailyPlayRepository.save(new AcornDailyPlay(1L, TODAY));
        }

        assertThat(acornDailyPlayRepository.countByUserIdAndPlayDate(1L, TODAY))
                .isEqualTo(AcornService.DAILY_PLAY_LIMIT);
    }
}
