package com.stayworld.back.acorn.controller;

import com.stayworld.back.acorn.dto.AcornHistoryResponse;
import com.stayworld.back.acorn.dto.AcornMeResponse;
import com.stayworld.back.acorn.dto.GamePlayResponse;
import com.stayworld.back.acorn.service.AcornService;
import com.stayworld.back.global.auth.LoginMemberArgumentResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link AcornController} HTTP 레이어 테스트.
 * 세션 유무에 따른 {@code @LoginMember} 동작(401)과, 서비스 예외가 응답 JSON(400)으로
 * 어떻게 매핑되는지를 {@link com.stayworld.back.global.exception.GlobalExceptionHandler} 까지 통째로 검증한다.
 */
@WebMvcTest(AcornController.class)
class AcornControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AcornService acornService;

    private MockHttpSession loginSession(long memberId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(LoginMemberArgumentResolver.SESSION_KEY, memberId);
        return session;
    }

    // ---- POST /games ----

    @Test
    void playGame_세션없으면_401() throws Exception {
        mockMvc.perform(post("/games"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    void playGame_정상이면_결과를_반환한다() throws Exception {
        when(acornService.play(1L)).thenReturn(new GamePlayResponse(80_000, 7, 7, 7));

        mockMvc.perform(post("/games").session(loginSession(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.acorns").value(80_000))
                .andExpect(jsonPath("$.data.first").value(7))
                .andExpect(jsonPath("$.data.second").value(7))
                .andExpect(jsonPath("$.data.third").value(7));
    }

    @Test
    void playGame_오늘_이미_참여했으면_400() throws Exception {
        when(acornService.play(1L)).thenThrow(new IllegalArgumentException("오늘은 이미 게임에 참여했습니다."));

        mockMvc.perform(post("/games").session(loginSession(1L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("오늘은 이미 게임에 참여했습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    // ---- GET /acorns/history ----

    @Test
    void getHistory_세션없으면_401() throws Exception {
        mockMvc.perform(get("/acorns/history"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getHistory_정상이면_내역을_반환한다() throws Exception {
        var item = new AcornHistoryResponse.Item("RESERVATION", -30_000, 20_000, LocalDateTime.of(2026, 8, 31, 12, 0));
        when(acornService.history(1L)).thenReturn(new AcornHistoryResponse(List.of(item)));

        mockMvc.perform(get("/acorns/history").session(loginSession(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.history[0].reason").value("RESERVATION"))
                .andExpect(jsonPath("$.data.history[0].amount").value(-30_000))
                .andExpect(jsonPath("$.data.history[0].balance").value(20_000));
    }

    // ---- GET /acorns/me ----

    @Test
    void getMyAcorn_세션없으면_401() throws Exception {
        mockMvc.perform(get("/acorns/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getMyAcorn_정상이면_잔액과_참여여부를_반환한다() throws Exception {
        when(acornService.me(1L)).thenReturn(new AcornMeResponse(50_000, true));

        mockMvc.perform(get("/acorns/me").session(loginSession(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balance").value(50_000))
                .andExpect(jsonPath("$.data.participated").value(true));
    }
}
