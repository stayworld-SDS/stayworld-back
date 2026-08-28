package com.stayworld.back.acorn.service;

import com.stayworld.back.acorn.dto.GamePlayResponse;
import com.stayworld.back.acorn.repository.AcornDailyPlayRepository;
import com.stayworld.back.acorn.repository.AcornHistoryRepository;
import com.stayworld.back.acorn.support.GameRandom;
import com.stayworld.back.acorn.support.SlotGame;
import com.stayworld.back.user.entity.User;
import com.stayworld.back.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcornServiceTest {

    @Mock
    AcornLedger acornLedger;
    @Mock
    AcornHistoryRepository acornHistoryRepository;
    @Mock
    AcornDailyPlayRepository acornDailyPlayRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    GameRandom gameRandom;
    @InjectMocks
    AcornService acornService;

    @Test
    void play_오늘_이미_참여했으면_400_예외이고_정산하지_않는다() {
        when(acornDailyPlayRepository.existsByUserIdAndPlayDate(eq(1L), any(LocalDate.class)))
                .thenReturn(true);

        assertThatThrownBy(() -> acornService.play(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미");

        verify(acornDailyPlayRepository, never()).saveAndFlush(any());
        verifyNoInteractions(acornLedger);
    }

    @Test
    void play_777이면_릴값과_정산_후_잔액을_반환한다() {
        when(acornDailyPlayRepository.existsByUserIdAndPlayDate(eq(1L), any())).thenReturn(false);
        when(gameRandom.roll()).thenReturn(new int[] {7, 7, 7});
        when(acornLedger.settleGameResult(1L, 7777, SlotGame.JACKPOT)).thenReturn(7877);

        GamePlayResponse res = acornService.play(1L);

        assertThat(res.first()).isEqualTo(7);
        assertThat(res.second()).isEqualTo(7);
        assertThat(res.third()).isEqualTo(7);
        assertThat(res.acorns()).isEqualTo(7877);
        verify(acornDailyPlayRepository).saveAndFlush(any());
    }

    @Test
    void play_전부_다른_숫자면_손실로_정산한다() {
        when(acornDailyPlayRepository.existsByUserIdAndPlayDate(eq(1L), any())).thenReturn(false);
        when(gameRandom.roll()).thenReturn(new int[] {1, 2, 3});
        when(acornLedger.settleGameResult(1L, -50, SlotGame.LOSE)).thenReturn(50);

        GamePlayResponse res = acornService.play(1L);

        assertThat(res.acorns()).isEqualTo(50);
        verify(acornLedger).settleGameResult(1L, -50, SlotGame.LOSE);
    }

    @Test
    void me_현재_잔액과_오늘_참여여부를_반환한다() {
        User user = new User();
        user.setBalance(120);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(acornDailyPlayRepository.existsByUserIdAndPlayDate(eq(1L), any())).thenReturn(true);

        var res = acornService.me(1L);

        assertThat(res.balance()).isEqualTo(120);
        assertThat(res.participated()).isTrue();
    }
}
