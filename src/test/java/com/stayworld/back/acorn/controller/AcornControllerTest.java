package com.stayworld.back.acorn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stayworld.back.acorn.dto.AcornHistoryResponse;
import com.stayworld.back.acorn.dto.AcornMeResponse;
import com.stayworld.back.acorn.dto.GamePlayRequest;
import com.stayworld.back.acorn.dto.GamePlayResponse;
import com.stayworld.back.acorn.service.AcornService;
import com.stayworld.back.global.auth.LoginMemberArgumentResolver;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link AcornController} HTTP 레이어 테스트.
 * 세션 유무에 따른 {@code @LoginMember} 동작(401), {@code @Valid} 동작(400), 서비스 예외가
 * 응답 JSON(400)으로 어떻게 매핑되는지를 {@link com.stayworld.back.global.exception.GlobalExceptionHandler}
 * 까지 통째로 검증한다.
 */
@WebMvcTest(AcornController.class)
class AcornControllerTest {

    @Autowired
    MockMvc mockMvc;

    // 요청 바디 직렬화용. 슬라이스 컨텍스트에 Jackson 자동 설정이 안 딸려와서 직접 만든다.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    AcornService acornService;

    private MockHttpSession loginSession(long memberId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(LoginMemberArgumentResolver.SESSION_KEY, memberId);
        return session;
    }

    private String body(int winAmount) throws Exception {
        return objectMapper.writeValueAsString(new GamePlayRequest(winAmount));
    }

    // ---- POST /games ----

    @Test
    void playGame_세션없으면_401() throws Exception {
        mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(0)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    void playGame_정상이면_결과를_반환한다() throws Exception {
        when(acornService.play(1L, 500)).thenReturn(new GamePlayResponse(10_400));

        mockMvc.perform(post("/games")
                        .session(loginSession(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(500)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.acorns").value(10_400));
    }

    @Test
    void playGame_획득량이_음수면_400() throws Exception {
        mockMvc.perform(post("/games")
                        .session(loginSession(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(-1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void playGame_오늘_참여횟수_상한이면_400() throws Exception {
        when(acornService.play(1L, 0))
                .thenThrow(new IllegalArgumentException("오늘 게임 참여 횟수(10회)를 모두 사용했습니다."));

        mockMvc.perform(post("/games")
                        .session(loginSession(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("오늘 게임 참여 횟수(10회)를 모두 사용했습니다."))
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
    void getHistory_정상이면_내역과_페이지_정보를_반환한다() throws Exception {
        var item = new AcornHistoryResponse.Item("RESERVATION", -30_000, 20_000, LocalDateTime.of(2026, 8, 31, 12, 0));
        when(acornService.history(eq(1L), any()))
                .thenReturn(new AcornHistoryResponse(List.of(item), 0, 20, 1, 1, false));

        mockMvc.perform(get("/acorns/history").session(loginSession(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.history[0].reason").value("RESERVATION"))
                .andExpect(jsonPath("$.data.history[0].amount").value(-30_000))
                .andExpect(jsonPath("$.data.history[0].balance").value(20_000))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    void getHistory_page_size_쿼리파라미터가_그대로_전달된다() throws Exception {
        when(acornService.history(eq(1L), any()))
                .thenReturn(new AcornHistoryResponse(List.of(), 2, 5, 11, 3, false));

        mockMvc.perform(get("/acorns/history?page=2&size=5").session(loginSession(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(2))
                .andExpect(jsonPath("$.data.size").value(5));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(acornService).history(eq(1L), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
    }

    // ---- GET /acorns/me ----

    @Test
    void getMyAcorn_세션없으면_401() throws Exception {
        mockMvc.perform(get("/acorns/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getMyAcorn_정상이면_잔액과_참여횟수_상한을_반환한다() throws Exception {
        when(acornService.me(1L)).thenReturn(new AcornMeResponse(50_000, 4, 10));

        mockMvc.perform(get("/acorns/me").session(loginSession(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balance").value(50_000))
                .andExpect(jsonPath("$.data.playCount").value(4))
                .andExpect(jsonPath("$.data.dailyLimit").value(10));
    }
}
