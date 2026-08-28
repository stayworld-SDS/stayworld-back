package com.stayworld.back.reservation.service;

import com.stayworld.back.global.exception.NotFoundException;
import com.stayworld.back.reservation.dto.ReservationCreateRequest;
import com.stayworld.back.reservation.dto.ReservationDetailResponse;
import com.stayworld.back.reservation.dto.ReservationSummaryResponse;
import com.stayworld.back.reservation.entity.Reservation;
import com.stayworld.back.reservation.repository.ReservationRepository;
import com.stayworld.back.reservation.support.GuesthouseInfo;
import com.stayworld.back.reservation.support.GuesthouseReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    ReservationRepository reservationRepository;
    @Mock
    GuesthouseReader guesthouseReader;
    @InjectMocks
    ReservationService reservationService;

    private static final LocalDate IN = LocalDate.now().plusDays(3);
    private static final LocalDate OUT = LocalDate.now().plusDays(6);   // 3박

    private GuesthouseInfo guesthouse(long id, int price, int capacity) {
        return new GuesthouseInfo(id, "게하" + id, price, "서울시 어딘가", capacity, true, true, false);
    }

    private Reservation reservation(Long id, long userId, long guesthouseId) {
        Reservation r = Reservation.builder()
                .userId(userId).guesthouseId(guesthouseId)
                .startDate(IN).endDate(OUT).headcount(2).cost(30_000)
                .build();
        ReflectionTestUtils.setField(r, "id", id);
        return r;
    }

    private ReservationCreateRequest request(long guesthouseId, LocalDate in, LocalDate out, int headcount) {
        return new ReservationCreateRequest(guesthouseId, in, out, headcount);
    }

    // ---- getMyReservations ----

    @Test
    void getMyReservations_숙소정보와_함께_요약을_반환한다() {
        when(reservationRepository.findByUserIdAndEndDateGreaterThanEqualOrderByStartDateAsc(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(reservation(10L, 1L, 100L), reservation(11L, 1L, 200L)));
        when(guesthouseReader.readAll(any())).thenReturn(Map.of(
                100L, guesthouse(100L, 10_000, 4),
                200L, guesthouse(200L, 20_000, 4)));

        List<ReservationSummaryResponse> result = reservationService.getMyReservations(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).reservationId()).isEqualTo(10L);
        assertThat(result.get(0).guesthouseName()).isEqualTo("게하100");
        assertThat(result.get(1).guesthouseId()).isEqualTo(200L);
    }

    // ---- getReservation ----

    @Test
    void getReservation_본인_예약이면_상세를_반환한다() {
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation(10L, 1L, 100L)));
        when(guesthouseReader.read(100L)).thenReturn(guesthouse(100L, 10_000, 4));

        ReservationDetailResponse res = reservationService.getReservation(10L, 1L);

        assertThat(res.guesthouseId()).isEqualTo(100L);
        assertThat(res.cost()).isEqualTo(30_000);
        assertThat(res.capacity()).isEqualTo(4);
        assertThat(res.parking()).isTrue();
    }

    @Test
    void getReservation_없는_예약이면_404() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getReservation(99L, 1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getReservation_남의_예약이면_404이고_숙소는_조회하지_않는다() {
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation(10L, 1L, 100L)));

        assertThatThrownBy(() -> reservationService.getReservation(10L, 2L))
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(guesthouseReader);
    }

    // ---- create ----

    @Test
    void create_정상이면_비용을_계산해_저장하고_id를_반환한다() {
        when(guesthouseReader.read(100L)).thenReturn(guesthouse(100L, 10_000, 4));
        when(reservationRepository.existsByGuesthouseIdAndStartDateLessThanAndEndDateGreaterThan(eq(100L), any(), any()))
                .thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 50L);
            return r;
        });

        Long id = reservationService.create(1L, request(100L, IN, OUT, 2));

        assertThat(id).isEqualTo(50L);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());
        Reservation saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getGuesthouseId()).isEqualTo(100L);
        assertThat(saved.getHeadcount()).isEqualTo(2);
        assertThat(saved.getCost()).isEqualTo(30_000);   // 10,000 * 3박
    }

    @Test
    void create_체크아웃이_체크인보다_뒤가_아니면_400() {
        when(guesthouseReader.read(100L)).thenReturn(guesthouse(100L, 10_000, 4));

        assertThatThrownBy(() -> reservationService.create(1L, request(100L, IN, IN, 2)))
                .isInstanceOf(IllegalArgumentException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void create_인원이_정원을_초과하면_400() {
        when(guesthouseReader.read(100L)).thenReturn(guesthouse(100L, 10_000, 2));

        assertThatThrownBy(() -> reservationService.create(1L, request(100L, IN, OUT, 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수용 인원");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void create_기간이_겹치면_400() {
        when(guesthouseReader.read(100L)).thenReturn(guesthouse(100L, 10_000, 4));
        when(reservationRepository.existsByGuesthouseIdAndStartDateLessThanAndEndDateGreaterThan(eq(100L), any(), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> reservationService.create(1L, request(100L, IN, OUT, 2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 예약");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void create_없는_숙소면_404() {
        when(guesthouseReader.read(100L)).thenThrow(new NotFoundException("숙소를 찾을 수 없습니다."));

        assertThatThrownBy(() -> reservationService.create(1L, request(100L, IN, OUT, 2)))
                .isInstanceOf(NotFoundException.class);

        verify(reservationRepository, never()).save(any());
    }

    // ---- delete ----

    @Test
    void delete_본인_예약이면_삭제한다() {
        Reservation r = reservation(10L, 1L, 100L);
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(r));

        reservationService.delete(10L, 1L);

        verify(reservationRepository).delete(r);
    }

    @Test
    void delete_남의_예약이면_404이고_삭제하지_않는다() {
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation(10L, 1L, 100L)));

        assertThatThrownBy(() -> reservationService.delete(10L, 2L))
                .isInstanceOf(NotFoundException.class);

        verify(reservationRepository, never()).delete(any());
    }
}
