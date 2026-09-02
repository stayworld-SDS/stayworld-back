package com.stayworld.back.wave.service;

import com.stayworld.back.acorn.entity.AcornHistory;
import com.stayworld.back.acorn.repository.AcornHistoryRepository;
import com.stayworld.back.acorn.service.AcornLedger;
import com.stayworld.back.profile.service.ProfileVisitService;
import com.stayworld.back.user.entity.User;
import com.stayworld.back.user.repository.UserRepository;
import com.stayworld.back.wave.dto.WaveResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 파도타기 첫 방문 보상이 실제로 {@code acorn_history} 원장에 남는지 실제 DB(H2)로 확인한다
 * ({@link AcornLedger} 를 목이 아닌 진짜로 태워서).
 */
@DataJpaTest
@Import({AcornLedger.class, ProfileVisitService.class, WaveService.class})
class WaveServiceAcornHistoryIntegrationTest {

    @Autowired UserRepository userRepository;
    @Autowired AcornHistoryRepository acornHistoryRepository;
    @Autowired WaveService waveService;

    @Test
    void 그날_첫_파도타기_보상이_acorn_history에_기록된다() {
        long me = newUser(50_000);
        long target = newUser(50_000);

        WaveResponse res = waveService.visit(me, target);

        assertThat(res.rewardedAcorns()).isEqualTo(1);
        assertThat(res.acornBalance()).isEqualTo(50_001);
        assertThat(userRepository.findById(me).orElseThrow().getBalance()).isEqualTo(50_001);

        var history = acornHistoryRepository
                .findByUserId(me, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
                .getContent();
        assertThat(history).hasSize(1);
        AcornHistory row = history.get(0);
        assertThat(row.getAmount()).isEqualTo(1);
        assertThat(row.getBalanceAfter()).isEqualTo(50_001);
        assertThat(row.getReason()).isEqualTo("파도타기 보상");
        assertThat(row.getCreatedAt()).isNotNull();
    }

    @Test
    void 같은_날_두번째_이후_파도타기는_원장에_쌓이지_않는다() {
        long me = newUser(50_000);
        long t1 = newUser(50_000);
        long t2 = newUser(50_000);

        waveService.visit(me, t1);
        waveService.visit(me, t2);

        assertThat(acornHistoryRepository.findByUserId(me, PageRequest.of(0, 10)).getTotalElements())
                .isEqualTo(1);
        assertThat(userRepository.findById(me).orElseThrow().getBalance()).isEqualTo(50_001);
    }

    private long newUser(int balance) {
        User u = new User();
        u.setEmail("u" + System.nanoTime() + "@test.com");
        u.setPassword("x");
        u.setNickname("유저");
        u.setPhoneNumber("010-0000-0000");
        u.setBalance(balance);
        u.setVisitorCount(0);
        u.setCreatedAt(LocalDateTime.now());
        return userRepository.save(u).getId();
    }
}
