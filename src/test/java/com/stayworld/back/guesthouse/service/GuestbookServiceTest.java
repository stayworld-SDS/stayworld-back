package com.stayworld.back.guesthouse.service;

import com.stayworld.back.global.exception.NotFoundException;
import com.stayworld.back.guesthouse.dto.GuestbookCreateRequest;
import com.stayworld.back.guesthouse.dto.GuestbookSummaryDto;
import com.stayworld.back.guesthouse.dto.GuestbookPageResponse;
import com.stayworld.back.guesthouse.entity.Guestbook;
import com.stayworld.back.guesthouse.entity.Guesthouse;
import com.stayworld.back.guesthouse.exception.GuesthouseNotFoundException;
import com.stayworld.back.guesthouse.exception.GuestbookEligibilityException;
import com.stayworld.back.guesthouse.repository.GuestbookRepository;
import com.stayworld.back.guesthouse.repository.GuesthouseRepository;
import com.stayworld.back.user.entity.User;
import com.stayworld.back.user.repository.UserRepository;
import com.stayworld.back.reservation.entity.Reservation;
import com.stayworld.back.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuestbookServiceTest {

    @Mock
    GuestbookRepository guestbookRepository;
    @Mock
    GuesthouseRepository guesthouseRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ReservationRepository reservationRepository;
    @InjectMocks
    GuestbookService guestbookService;

    private Guestbook guestbook(String writer, LocalDateTime createdAt) {
        User user = new User();
        user.setNickname(writer);
        Guestbook guestbook = new Guestbook();
        guestbook.setUser(user);
        guestbook.setBody(writer + "의 방명록입니다.");
        guestbook.setCreatedAt(createdAt);
        return guestbook;
    }

    @Test
    void findByGuesthouseId_최신순으로_방명록을_반환한다() {
        LocalDateTime now = LocalDateTime.now();
        when(guesthouseRepository.existsById(1L)).thenReturn(true);
        when(guestbookRepository.findByGuesthouseId(eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(
                guestbook("세번째", now),
                guestbook("두번째", now.minusDays(1)),
                guestbook("첫번째", now.minusDays(2)))));

        GuestbookPageResponse result = guestbookService.findByGuesthouseId(1L, 0);

        assertThat(result.guestbooks()).extracting(GuestbookSummaryDto::getWriter)
                .containsExactly("세번째", "두번째", "첫번째");
    }

    @Test
    void saveGuestbook_정상이면_작성자와_숙소와_본문을_저장한다() {
        User user = new User();
        user.setNickname("작성자");
        Guesthouse guesthouse = new Guesthouse();
        guesthouse.setId(10L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(guesthouseRepository.findById(10L)).thenReturn(Optional.of(guesthouse));
        guestbookService.saveGuestbook(1L, 10L, new GuestbookCreateRequest("열 글자 이상의 방명록 본문입니다."));

        ArgumentCaptor<Guestbook> captor = ArgumentCaptor.forClass(Guestbook.class);
        verify(guestbookRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue()).satisfies(saved -> {
            assertThat(saved.getUser()).isSameAs(user);
            assertThat(saved.getGuesthouse()).isSameAs(guesthouse);
            assertThat(saved.getBody()).isEqualTo("열 글자 이상의 방명록 본문입니다.");
        });
    }

    @Test
    void saveGuestbook_없는_유저면_404이고_저장하지_않는다() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> guestbookService.saveGuestbook(
                999L, 10L, new GuestbookCreateRequest("열 글자 이상의 방명록 본문입니다.")))
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(guesthouseRepository);
        verify(guestbookRepository, never()).save(any());
    }

    @Test
    void saveGuestbook_없는_숙소면_404이고_저장하지_않는다() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(guesthouseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> guestbookService.saveGuestbook(
                1L, 999L, new GuestbookCreateRequest("열 글자 이상의 방명록 본문입니다.")))
                .isInstanceOf(GuesthouseNotFoundException.class);

        verify(guestbookRepository, never()).save(any());
    }

    @Test
    void findByGuesthouseId_없는_숙소면_404() {
        when(guesthouseRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> guestbookService.findByGuesthouseId(999L, 0))
                .isInstanceOf(GuesthouseNotFoundException.class);

        verifyNoInteractions(guestbookRepository);
    }

    @Test
    void findByGuesthouseId_방명록이_없으면_빈_목록을_반환한다() {
        when(guesthouseRepository.existsById(1L)).thenReturn(true);
        when(guestbookRepository.findByGuesthouseId(eq(1L), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThat(guestbookService.findByGuesthouseId(1L, 0).guestbooks()).isEmpty();
    }

    @Test
    void findByGuesthouseId_페이지크기는_5이고_최신순으로_조회한다() {
        when(guesthouseRepository.existsById(1L)).thenReturn(true);
        when(guestbookRepository.findByGuesthouseId(eq(1L), any(Pageable.class)))
                .thenReturn(Page.empty());

        guestbookService.findByGuesthouseId(1L, 2);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(guestbookRepository).findByGuesthouseId(eq(1L), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
        assertThat(captor.getValue().getSort().getOrderFor("createdAt").getDirection())
                .isEqualTo(Sort.Direction.DESC);
        assertThat(captor.getValue().getSort().getOrderFor("id").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void findByGuesthouseId_음수_페이지면_400() {
        assertThatThrownBy(() -> guestbookService.findByGuesthouseId(1L, -1))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(guesthouseRepository, guestbookRepository);
    }
}
