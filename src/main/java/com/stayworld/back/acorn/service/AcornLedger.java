package com.stayworld.back.acorn.service;

import com.stayworld.back.acorn.entity.AcornHistory;
import com.stayworld.back.acorn.exception.InsufficientAcornException;
import com.stayworld.back.acorn.repository.AcornHistoryRepository;
import com.stayworld.back.global.exception.NotFoundException;
import com.stayworld.back.user.entity.User;
import com.stayworld.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 도토리 잔액 변경의 단일 창구. 게임뿐 아니라 예약 결제 등 다른 도메인도
 * 도토리를 쓰거나 줄 때 이 클래스를 호출한다 (엔티티/원장을 직접 만지지 않는다).
 *
 * <p>잔액은 현재 {@code users.balance} 에 있고 user 도메인 소유라 여기서는 읽고/써준다.
 * 모든 메서드는 호출자의 {@code @Transactional} 안에서 실행되어야 한다.
 *
 * <p>TODO(동시성): load-modify-save 라 같은 유저에 동시 요청이 겹치면 갱신 유실 가능.
 * 추후 {@code UPDATE users SET balance = balance + :d} 원자 쿼리나 {@code @Version} 로 보강.
 */
@Service
@RequiredArgsConstructor
public class AcornLedger {

    private final UserRepository userRepository;
    private final AcornHistoryRepository acornHistoryRepository;

    /**
     * 도토리 지급. {@code amount} 는 0 이상.
     *
     * @return 지급 후 잔액
     */
    @Transactional
    public int earn(Long userId, int amount, String reason) {
        if (amount < 0) {
            throw new IllegalArgumentException("지급액은 음수일 수 없습니다.");
        }
        return applyDelta(userId, amount, reason);
    }

    /**
     * 도토리 차감. {@code amount} 는 0 이상. 잔액이 부족하면
     * {@link InsufficientAcornException} 을 던진다 (음수/부분 차감 없음).
     *
     * @return 차감 후 잔액
     */
    @Transactional
    public int spend(Long userId, int amount, String reason) {
        if (amount < 0) {
            throw new IllegalArgumentException("차감액은 음수일 수 없습니다.");
        }
        return applyDelta(userId, -amount, reason);
    }

    private int applyDelta(Long userId, int delta, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다."));

        int before = user.getBalance();
        int after = before + delta;
        if (after < 0) {
            throw new InsufficientAcornException(before, -delta);
        }

        user.setBalance(after);          // 더티 체킹으로 커밋 시 반영

        acornHistoryRepository.save(AcornHistory.builder()
                .userId(userId)
                .amount(delta)
                .balanceAfter(after)
                .reason(reason)
                .build());

        return after;
    }
}
