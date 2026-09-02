package com.stayworld.back.wave.service;

import com.stayworld.back.acorn.service.AcornLedger;
import com.stayworld.back.global.exception.NotFoundException;
import com.stayworld.back.profile.service.ProfileVisitService;
import com.stayworld.back.user.entity.User;
import com.stayworld.back.user.repository.UserRepository;
import com.stayworld.back.wave.dto.WaveMeResponse;
import com.stayworld.back.wave.dto.WaveResponse;
import com.stayworld.back.wave.entity.WaveDailyLog;
import com.stayworld.back.wave.repository.WaveDailyLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WaveServiceTest {

    @Mock
    WaveDailyLogRepository waveDailyLogRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    AcornLedger acornLedger;
    @Mock
    ProfileVisitService profileVisitService;
    @InjectMocks
    WaveService waveService;

    @Test
    void 그날_첫_파도타기면_도토리를_지급하고_방문_이벤트를_기록한다() {
        when(waveDailyLogRepository.countByUserIdAndWaveDate(eq(1L), any())).thenReturn(0L);
        when(acornLedger.earn(eq(1L), eq(1), anyString())).thenReturn(51);

        WaveResponse res = waveService.visit(1L, 2L);

        assertThat(res.rewardedAcorns()).isEqualTo(1);
        assertThat(res.acornBalance()).isEqualTo(51);
        assertThat(res.wavesToday()).isEqualTo(1);
        verify(profileVisitService).recordVisit(2L, 1L);   // (owner, visitor)

        ArgumentCaptor<WaveDailyLog> captor = ArgumentCaptor.forClass(WaveDailyLog.class);
        verify(waveDailyLogRepository).save(captor.capture());
        assertThat(captor.getValue().isRewarded()).isTrue();
        assertThat(captor.getValue().getTargetUserId()).isEqualTo(2L);
    }

    @Test
    void 그날_두번째_이후_파도타기는_보상이_없다() {
        User me = new User();
        me.setBalance(50);
        when(waveDailyLogRepository.countByUserIdAndWaveDate(eq(1L), any())).thenReturn(3L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(me));

        WaveResponse res = waveService.visit(1L, 2L);

        assertThat(res.rewardedAcorns()).isZero();
        assertThat(res.acornBalance()).isEqualTo(50);
        assertThat(res.wavesToday()).isEqualTo(4);
        verify(profileVisitService).recordVisit(2L, 1L);
        verify(acornLedger, never()).earn(anyLong(), anyInt(), anyString());
    }

    @Test
    void 본인_미니홈피로는_파도탈_수_없다() {
        assertThatThrownBy(() -> waveService.visit(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class);

        verify(profileVisitService, never()).recordVisit(any(), any());
    }

    @Test
    void 하루_제한을_넘으면_거부되고_아무것도_안_바뀐다() {
        when(waveDailyLogRepository.countByUserIdAndWaveDate(eq(1L), any())).thenReturn(20L);

        assertThatThrownBy(() -> waveService.visit(1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("20");

        verify(profileVisitService, never()).recordVisit(any(), any());
        verify(waveDailyLogRepository, never()).save(any());
    }

    @Test
    void 없는_유저로_파도타면_방문_이벤트에서_404() {
        when(waveDailyLogRepository.countByUserIdAndWaveDate(eq(1L), any())).thenReturn(0L);
        doThrow(new NotFoundException("유저를 찾을 수 없습니다."))
                .when(profileVisitService).recordVisit(99L, 1L);

        assertThatThrownBy(() -> waveService.visit(1L, 99L))
                .isInstanceOf(NotFoundException.class);

        verify(waveDailyLogRepository, never()).save(any());
    }

    @Test
    void today_오늘_현황을_돌려준다() {
        when(waveDailyLogRepository.countByUserIdAndWaveDate(eq(1L), any())).thenReturn(5L);

        WaveMeResponse me = waveService.today(1L);

        assertThat(me.wavesToday()).isEqualTo(5);
        assertThat(me.rewardClaimed()).isTrue();
        assertThat(me.dailyLimit()).isEqualTo(20);
    }
}
