package com.stayworld.back.guesthouse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stayworld.back.guesthouse.dto.GuestbookCreateRequest;
import com.stayworld.back.guesthouse.dto.GuestbookSummaryDto;
import com.stayworld.back.guesthouse.dto.GuestbookPageResponse;
import com.stayworld.back.guesthouse.dto.GuesthouseDto;
import com.stayworld.back.guesthouse.exception.GuesthouseNotFoundException;
import com.stayworld.back.guesthouse.service.GuestbookService;
import com.stayworld.back.guesthouse.service.GuesthouseService;
import com.stayworld.back.global.auth.LoginMemberArgumentResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GuesthouseController.class)
class
GuesthouseControllerTest {

    @Autowired
    MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    GuestbookService guestbookService;
    @MockitoBean
    GuesthouseService guesthouseService;

    private String guestbookBody(int length) throws Exception {
        return objectMapper.writeValueAsString(new GuestbookCreateRequest("가".repeat(length)));
    }

    private MockHttpSession loginSession(long userId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(LoginMemberArgumentResolver.SESSION_KEY, userId);
        return session;
    }

    @Test
    void postGuestbook_본문이_9자이면_400() throws Exception {
        mockMvc.perform(post("/guesthouses/1/guestbooks")
                        .session(loginSession(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(guestbookBody(9)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postGuestbook_본문이_10자이면_저장한다() throws Exception {
        mockMvc.perform(post("/guesthouses/1/guestbooks")
                        .session(loginSession(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(guestbookBody(10)))
                .andExpect(status().isOk());

        verify(guestbookService).saveGuestbook(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(1L),
                argThat(request -> request.getBody().length() == 10));
    }

    @Test
    void postGuestbook_한글_본문이_500자이면_저장한다() throws Exception {
        mockMvc.perform(post("/guesthouses/1/guestbooks")
                        .session(loginSession(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(guestbookBody(500)))
                .andExpect(status().isOk());

        verify(guestbookService).saveGuestbook(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(1L),
                argThat(request -> request.getBody().length() == 500));
    }

    @Test
    void postGuestbook_본문이_501자이면_400() throws Exception {
        mockMvc.perform(post("/guesthouses/1/guestbooks")
                        .session(loginSession(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(guestbookBody(501)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postGuestbook_세션이_없으면_401() throws Exception {
        mockMvc.perform(post("/guesthouses/1/guestbooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(guestbookBody(10)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getGuesthouse_정상이면_숙소정보를_반환한다() throws Exception {
        GuesthouseDto guesthouse = new GuesthouseDto();
        guesthouse.setId(1L);
        guesthouse.setName("서울 게스트하우스");
        guesthouse.setMusic("playlist");
        when(guesthouseService.findById(1L)).thenReturn(guesthouse);

        mockMvc.perform(get("/guesthouses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("서울 게스트하우스"))
                .andExpect(jsonPath("$.data.music").value("playlist"));
    }

    @Test
    void getGuesthouse_없는_숙소면_404() throws Exception {
        when(guesthouseService.findById(999L)).thenThrow(new GuesthouseNotFoundException());

        mockMvc.perform(get("/guesthouses/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void searchAvailableGuesthouses_검색조건을_서비스에_전달한다() throws Exception {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(2);
        when(guesthouseService.searchAvailableGuesthouses("서울", start, end, 2))
                .thenReturn(List.of());

        mockMvc.perform(get("/guesthouses")
                        .param("location", "서울")
                        .param("start", start.toString())
                        .param("end", end.toString())
                        .param("headCount", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        verify(guesthouseService).searchAvailableGuesthouses("서울", start, end, 2);
    }

    @Test
    void getGuestbook_정상이면_방명록_목록을_반환한다() throws Exception {
        GuestbookSummaryDto guestbook = new GuestbookSummaryDto();
        guestbook.setWriter("작성자");
        guestbook.setBody("열 글자 이상의 방명록 본문입니다.");
        guestbook.setCreatedAt(LocalDateTime.of(2026, 9, 1, 12, 0));
        when(guestbookService.findByGuesthouseId(1L, 0)).thenReturn(
                new GuestbookPageResponse(List.of(guestbook), 0, 5, 1, 1, false));

        mockMvc.perform(get("/guesthouses/1/guestbooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.guestbooks[0].writer").value("작성자"))
                .andExpect(jsonPath("$.data.guestbooks[0].body").value("열 글자 이상의 방명록 본문입니다."))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }
}
