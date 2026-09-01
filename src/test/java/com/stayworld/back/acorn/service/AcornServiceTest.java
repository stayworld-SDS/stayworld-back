package com.stayworld.back.acorn.service;

import com.stayworld.back.acorn.dto.AcornHistoryResponse;
import com.stayworld.back.acorn.dto.GamePlayResponse;
import com.stayworld.back.acorn.entity.AcornHistory;
import com.stayworld.back.acorn.repository.AcornDailyPlayRepository;
import com.stayworld.back.acorn.repository.AcornHistoryRepository;
import com.stayworld.back.user.entity.User;
import com.stayworld.back.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
    @InjectMocks
    AcornService acornService;

    @Test
    void play_오늘_참여횟수가_상한이면_400이고_정산하지_않는다() {
        when(acornDailyPlayRepository.countByUserIdAndPlayDate(eq(1L), any(LocalDate.class)))
                .thenReturn((long) AcornService.DAILY_PLAY_LIMIT);

        assertThatThrownBy(() -> acornService.play(1L, 500))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(AcornService.DAILY_PLAY_LIMIT + "회");

        verify(acornDailyPlayRepository, never()).save(any());
        verifyNoInteractions(acornLedger);
    }

    @Test
    void play_참여비를_차감하고_획득량이_있으면_추가로_지급한다() {
        when(acornDailyPlayRepository.countByUserIdAndPlayDate(eq(1L), any())).thenReturn(0L);
        when(acornLedger.spend(1L, 100, "GAME_ENTRY")).thenReturn(9_900);
        when(acornLedger.earn(1L, 500, "GAME_WIN")).thenReturn(10_400);

        GamePlayResponse res = acornService.play(1L, 500);

        assertThat(res.getAcorns()).isEqualTo(10_400);
        verify(acornDailyPlayRepository).save(any());
        verify(acornLedger).spend(1L, 100, "GAME_ENTRY");
        verify(acornLedger).earn(1L, 500, "GAME_WIN");
    }

    @Test
    void play_획득량이_0이면_참여비만_차감하고_지급은_없다() {
        when(acornDailyPlayRepository.countByUserIdAndPlayDate(eq(1L), any())).thenReturn(3L);
        when(acornLedger.spend(1L, 100, "GAME_ENTRY")).thenReturn(9_900);

        GamePlayResponse res = acornService.play(1L, 0);

        assertThat(res.getAcorns()).isEqualTo(9_900);
        verify(acornLedger, never()).earn(any(), anyInt(), any());
    }

    @Test
    void me_현재_잔액과_오늘_참여횟수_상한을_반환한다() {
        User user = new User();
        user.setBalance(120);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(acornDailyPlayRepository.countByUserIdAndPlayDate(eq(1L), any())).thenReturn(4L);

        var res = acornService.me(1L);

        assertThat(res.getBalance()).isEqualTo(120);
        assertThat(res.getPlayCount()).isEqualTo(4);
        assertThat(res.getDailyLimit()).isEqualTo(AcornService.DAILY_PLAY_LIMIT);
    }

    @Test
    void history_요청한_pageable_그대로_레포지토리에_전달한다() {
        AcornHistory h = AcornHistory.builder()
                .userId(1L).amount(-100).balanceAfter(9_900).reason("GAME_ENTRY").build();
        Pageable pageable = PageRequest.of(0, 20);
        when(acornHistoryRepository.findByUserId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(h), pageable, 1));

        AcornHistoryResponse res = acornService.history(1L, pageable);

        assertThat(res.getHistory()).hasSize(1);
        assertThat(res.getTotalElements()).isEqualTo(1);
        verify(acornHistoryRepository).findByUserId(1L, pageable);
    }

    @Test
    void history_size가_상한을_넘으면_상한으로_잘라서_조회한다() {
        Pageable requested = PageRequest.of(2, 9_999);
        when(acornHistoryRepository.findByUserId(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of()));

        acornService.history(1L, requested);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(acornHistoryRepository).findByUserId(eq(1L), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(100);   // MAX_HISTORY_PAGE_SIZE
        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);   // page 번호는 그대로
    }
}
