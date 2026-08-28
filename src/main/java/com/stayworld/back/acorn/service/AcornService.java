package com.stayworld.back.acorn.service;

import com.stayworld.back.acorn.dto.AcornHistoryResponse;
import com.stayworld.back.acorn.dto.AcornMeResponse;
import com.stayworld.back.acorn.dto.GamePlayResponse;
import com.stayworld.back.acorn.entity.AcornDailyPlay;
import com.stayworld.back.acorn.repository.AcornDailyPlayRepository;
import com.stayworld.back.acorn.repository.AcornHistoryRepository;
import com.stayworld.back.acorn.support.GameRandom;
import com.stayworld.back.acorn.support.SlotGame;
import com.stayworld.back.global.exception.NotFoundException;
import com.stayworld.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcornService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final AcornLedger acornLedger;
    private final AcornHistoryRepository acornHistoryRepository;
    private final AcornDailyPlayRepository acornDailyPlayRepository;
    private final UserRepository userRepository;
    private final GameRandom gameRandom;

    /** 777 슬롯 1회 플레이. 하루 1회 제한. */
    @Transactional
    public GamePlayResponse play(Long userId) {
        LocalDate today = LocalDate.now(KST);

        if (acornDailyPlayRepository.existsByUserIdAndPlayDate(userId, today)) {
            throw new IllegalArgumentException("오늘은 이미 게임에 참여했습니다.");
        }
        try {
            acornDailyPlayRepository.saveAndFlush(new AcornDailyPlay(userId, today));
        } catch (DataIntegrityViolationException e) {   // 동시 요청 레이스
            throw new IllegalArgumentException("오늘은 이미 게임에 참여했습니다.");
        }

        int[] reels = gameRandom.roll();
        SlotGame.Result result = SlotGame.evaluate(reels[0], reels[1], reels[2]);
        int balance = acornLedger.settleGameResult(userId, result.delta(), result.reason());

        return new GamePlayResponse(balance, reels[0], reels[1], reels[2]);
    }

    public AcornHistoryResponse history(Long userId) {
        return AcornHistoryResponse.from(acornHistoryRepository.findByUserIdOrderByIdDesc(userId));
    }

    public AcornMeResponse me(Long userId) {
        int balance = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다."))
                .getBalance();
        boolean participated = acornDailyPlayRepository
                .existsByUserIdAndPlayDate(userId, LocalDate.now(KST));
        return new AcornMeResponse(balance, participated);
    }
}
