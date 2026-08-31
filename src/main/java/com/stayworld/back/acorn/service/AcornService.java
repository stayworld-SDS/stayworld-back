package com.stayworld.back.acorn.service;

import com.stayworld.back.acorn.dto.AcornHistoryResponse;
import com.stayworld.back.acorn.dto.AcornMeResponse;
import com.stayworld.back.acorn.dto.GamePlayResponse;
import com.stayworld.back.acorn.entity.AcornDailyPlay;
import com.stayworld.back.acorn.repository.AcornDailyPlayRepository;
import com.stayworld.back.acorn.repository.AcornHistoryRepository;
import com.stayworld.back.global.exception.NotFoundException;
import com.stayworld.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcornService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /** 슬롯 1회 참여 고정 비용. 당첨 판정/연출은 프론트가 하고 획득량만 넘겨받는다. */
    private static final int ENTRY_FEE = 100;
    private static final String REASON_ENTRY = "GAME_ENTRY";
    private static final String REASON_WIN = "GAME_WIN";

    /** 하루 참여 제한. */
    public static final int DAILY_PLAY_LIMIT = 10;

    /** GET /acorns/history 한 페이지 최대 개수. 클라이언트가 더 큰 size 를 요청해도 이 이상은 안 준다. */
    private static final int MAX_HISTORY_PAGE_SIZE = 100;

    private final AcornLedger acornLedger;
    private final AcornHistoryRepository acornHistoryRepository;
    private final AcornDailyPlayRepository acornDailyPlayRepository;
    private final UserRepository userRepository;

    /**
     * 슬롯 1회 참여. 참여비({@value #ENTRY_FEE})를 차감하고, {@code winAmount} 가 있으면 지급한다.
     * 하루 {@value #DAILY_PLAY_LIMIT} 회 제한.
     *
     * @param winAmount 프론트에서 계산한 이번 판 획득량 (0 이상, 꽝이면 0)
     */
    @Transactional
    public GamePlayResponse play(Long userId, int winAmount) {
        LocalDate today = LocalDate.now(KST);

        long playCount = acornDailyPlayRepository.countByUserIdAndPlayDate(userId, today);
        if (playCount >= DAILY_PLAY_LIMIT) {
            throw new IllegalArgumentException("오늘 게임 참여 횟수(" + DAILY_PLAY_LIMIT + "회)를 모두 사용했습니다.");
        }
        acornDailyPlayRepository.save(new AcornDailyPlay(userId, today));

        int balance = acornLedger.spend(userId, ENTRY_FEE, REASON_ENTRY);
        if (winAmount > 0) {
            balance = acornLedger.earn(userId, winAmount, REASON_WIN);
        }

        return new GamePlayResponse(balance);
    }

    public AcornHistoryResponse history(Long userId, Pageable pageable) {
        Pageable capped = pageable.getPageSize() > MAX_HISTORY_PAGE_SIZE
                ? PageRequest.of(pageable.getPageNumber(), MAX_HISTORY_PAGE_SIZE, pageable.getSort())
                : pageable;
        return AcornHistoryResponse.from(acornHistoryRepository.findByUserId(userId, capped));
    }

    public AcornMeResponse me(Long userId) {
        int balance = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다."))
                .getBalance();
        long playCount = acornDailyPlayRepository.countByUserIdAndPlayDate(userId, LocalDate.now(KST));
        return new AcornMeResponse(balance, (int) playCount, DAILY_PLAY_LIMIT);
    }
}
