package com.stayworld.back.wave.service;

import com.stayworld.back.acorn.service.AcornLedger;
import com.stayworld.back.global.exception.NotFoundException;
import com.stayworld.back.profile.service.ProfileVisitService;
import com.stayworld.back.user.repository.UserRepository;
import com.stayworld.back.wave.dto.WaveMeResponse;
import com.stayworld.back.wave.dto.WaveResponse;
import com.stayworld.back.wave.entity.WaveDailyLog;
import com.stayworld.back.wave.repository.WaveDailyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * "파도타서 놀러가기". 추천 카드에서 한 명을 골라 미니홈피로 들어가는 행동을 기록한다.
 * 대상의 방문자 수를 올리고, 그날 첫 파도타기면 도토리를 지급한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WaveService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int DAILY_WAVE_LIMIT = 20;
    private static final int WAVE_REWARD = 1;
    private static final String REASON_REWARD = "파도타기 보상";

    private final WaveDailyLogRepository waveDailyLogRepository;
    private final UserRepository userRepository;
    private final AcornLedger acornLedger;
    private final ProfileVisitService profileVisitService;

    @Transactional
    public WaveResponse visit(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new IllegalArgumentException("본인 미니홈피로는 파도탈 수 없습니다.");
        }

        LocalDate today = LocalDate.now(KST);
        long wavesToday = waveDailyLogRepository.countByUserIdAndWaveDate(userId, today);
        if (wavesToday >= DAILY_WAVE_LIMIT) {
            throw new IllegalArgumentException("오늘 파도타기 횟수(" + DAILY_WAVE_LIMIT + "회)를 모두 사용했습니다.");
        }

        // 방문 이벤트 단일 창구. 대상이 없으면 여기서 404, 투데이 +1(오늘 첫 방문 시)도 여기서.
        profileVisitService.recordVisit(targetUserId, userId);

        boolean firstToday = wavesToday == 0;
        int rewarded = 0;
        int balance;
        if (firstToday) {
            balance = acornLedger.earn(userId, WAVE_REWARD, REASON_REWARD);
            rewarded = WAVE_REWARD;
        } else {
            balance = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다."))
                    .getBalance();
        }

        waveDailyLogRepository.save(new WaveDailyLog(userId, today, targetUserId, firstToday));
        return new WaveResponse(targetUserId, rewarded, balance, wavesToday + 1);
    }

    public WaveMeResponse today(Long userId) {
        long count = waveDailyLogRepository.countByUserIdAndWaveDate(userId, LocalDate.now(KST));
        return new WaveMeResponse(count, count > 0, DAILY_WAVE_LIMIT);
    }
}
