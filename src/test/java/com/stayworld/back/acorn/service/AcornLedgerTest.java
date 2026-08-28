package com.stayworld.back.acorn.service;

import com.stayworld.back.acorn.entity.AcornHistory;
import com.stayworld.back.acorn.exception.InsufficientAcornException;
import com.stayworld.back.acorn.repository.AcornHistoryRepository;
import com.stayworld.back.user.entity.User;
import com.stayworld.back.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcornLedgerTest {

    @Mock
    UserRepository userRepository;
    @Mock
    AcornHistoryRepository acornHistoryRepository;
    @InjectMocks
    AcornLedger acornLedger;

    User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setBalance(100);
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    }

    @Test
    void earn_잔액이_늘고_원장에_기록된다() {
        int after = acornLedger.earn(1L, 50, "SIGNUP_BONUS");

        assertThat(after).isEqualTo(150);
        assertThat(user.getBalance()).isEqualTo(150);

        ArgumentCaptor<AcornHistory> captor = ArgumentCaptor.forClass(AcornHistory.class);
        verify(acornHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualTo(50);
        assertThat(captor.getValue().getBalanceAfter()).isEqualTo(150);
        assertThat(captor.getValue().getReason()).isEqualTo("SIGNUP_BONUS");
    }

    @Test
    void spend_잔액이_충분하면_차감된다() {
        int after = acornLedger.spend(1L, 30, "RESERVATION");

        assertThat(after).isEqualTo(70);
        assertThat(user.getBalance()).isEqualTo(70);
    }

    @Test
    void spend_잔액이_부족하면_예외이고_아무것도_바뀌지_않는다() {
        assertThatThrownBy(() -> acornLedger.spend(1L, 150, "RESERVATION"))
                .isInstanceOf(InsufficientAcornException.class)
                .hasMessageContaining("보유 100")
                .hasMessageContaining("필요 150");

        assertThat(user.getBalance()).isEqualTo(100);
        verify(acornHistoryRepository, never()).save(any());
    }

    @Test
    void settleGameResult_손실이_잔액보다_커도_0에서_멈춘다() {
        user.setBalance(30);

        int after = acornLedger.settleGameResult(1L, -50, "GAME_LOSE");

        assertThat(after).isZero();

        ArgumentCaptor<AcornHistory> captor = ArgumentCaptor.forClass(AcornHistory.class);
        verify(acornHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualTo(-30);   // clamp 가 반영된 실제 증감
        assertThat(captor.getValue().getBalanceAfter()).isZero();
    }

    @Test
    void 음수_금액으로_earn_이나_spend_호출하면_거부된다() {
        assertThatThrownBy(() -> acornLedger.earn(1L, -10, "X"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> acornLedger.spend(1L, -10, "X"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(acornHistoryRepository, never()).save(any());
    }
}
