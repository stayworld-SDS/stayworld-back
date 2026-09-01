package com.stayworld.back.reservation.service;

import com.stayworld.back.acorn.exception.InsufficientAcornException;
import com.stayworld.back.acorn.service.AcornLedger;
import com.stayworld.back.global.exception.NotFoundException;
import com.stayworld.back.guesthouse.entity.Guesthouse;
import com.stayworld.back.guesthouse.repository.GuesthouseRepository;
import com.stayworld.back.reservation.dto.ReservationCreateRequest;
import com.stayworld.back.reservation.dto.ReservationDetailResponse;
import com.stayworld.back.reservation.dto.ReservationSummaryResponse;
import com.stayworld.back.reservation.entity.Reservation;
import com.stayworld.back.reservation.repository.DailyOccupancyRepository;
import com.stayworld.back.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
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
    DailyOccupancyRepository dailyOccupancyRepository;
    @Mock
    GuesthouseRepository guesthouseRepository;
    @Mock
    AcornLedger acornLedger;
    @InjectMocks
    ReservationService reservationService;

    private static final LocalDate IN = LocalDate.now().plusDays(3);
    private static final LocalDate OUT = LocalDate.now().plusDays(6);   // 3박

    private Guesthouse guesthouse(long id, int price, int capacity) {
        Guesthouse guesthouse = new Guesthouse();
        guesthouse.setId(id);
        guesthouse.setName("게하" + id);
        guesthouse.setPrice(price);
        guesthouse.setAddress("서울시 어딘가");
        guesthouse.setCapacity(capacity);
        guesthouse.setParkingProvided(true);
        guesthouse.setWifiProvided(true);
        guesthouse.setBreakfastProvided(false);
        guesthouse.setVisitorCount(2);
        return guesthouse;
    }

    private Reservation reservation(Long id, long userId, long guesthouseId) {
        Reservation r = Reservation.builder()
                .userId(userId).guesthouse(guesthouse(guesthouseId, 10_000, 4))
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

        List<ReservationSummaryResponse> result = reservationService.getMyReservations(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).reservationId()).isEqualTo(10L);
        assertThat(result.get(0).guesthouseName()).isEqualTo("게하100");
        assertThat(result.get(1).guesthouseId()).isEqualTo(200L);
    }

    // ---- getReservationHistory ----

    @Test
    void getReservationHistory_체크아웃이_지난_예약을_숙소정보와_함께_반환한다() {
        when(reservationRepository.findByUserIdAndEndDateLessThanOrderByStartDateDesc(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(reservation(10L, 1L, 100L)));

        List<ReservationSummaryResponse> result = reservationService.getReservationHistory(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).reservationId()).isEqualTo(10L);
        assertThat(result.get(0).guesthouseName()).isEqualTo("게하100");
    }

    // ---- getReservation ----

    @Test
    void getReservation_본인_예약이면_상세를_반환한다() {
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation(10L, 1L, 100L)));

        ReservationDetailResponse res = reservationService.getReservation(10L, 1L);

        assertThat(res.reservationId()).isEqualTo(10L);
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

        verifyNoInteractions(guesthouseRepository);
    }

    // ---- create ----

    @Test
    void create_정상이면_비용을_계산해_저장하고_id를_반환한다() {
        when(guesthouseRepository.findById(100L)).thenReturn(Optional.of(guesthouse(100L, 10_000, 4)));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 50L);
            return r;
        });

        Long id = reservationService.create(1L, request(100L, IN, OUT, 2));

        assertThat(id).isEqualTo(50L);

        verify(acornLedger).spend(1L, 30_000, "RESERVATION");   // 10,000 * 3박
        verify(dailyOccupancyRepository).increaseOccupancy(100L, IN, OUT, 2);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());
        Reservation saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getGuesthouse().getId()).isEqualTo(100L);
        assertThat(saved.getHeadcount()).isEqualTo(2);
        assertThat(saved.getCost()).isEqualTo(30_000);   // 10,000 * 3박
    }

    @Test
    void create_도토리_잔액이_부족하면_400이고_예약을_저장하지_않는다() {
        when(guesthouseRepository.findById(100L)).thenReturn(Optional.of(guesthouse(100L, 10_000, 4)));
        when(acornLedger.spend(1L, 30_000, "RESERVATION"))
                .thenThrow(new InsufficientAcornException(10_000, 30_000));

        assertThatThrownBy(() -> reservationService.create(1L, request(100L, IN, OUT, 2)))
                .isInstanceOf(InsufficientAcornException.class);

        verify(reservationRepository, never()).save(any());
        verify(dailyOccupancyRepository, never()).increaseOccupancy(100L, IN, OUT, 2);
    }

    @Test
    void create_체크아웃이_체크인보다_뒤가_아니면_400() {
        when(guesthouseRepository.findById(100L)).thenReturn(Optional.of(guesthouse(100L, 10_000, 4)));

        assertThatThrownBy(() -> reservationService.create(1L, request(100L, IN, IN, 2)))
                .isInstanceOf(IllegalArgumentException.class);

        verify(reservationRepository, never()).save(any());
        verifyNoInteractions(acornLedger);
    }

    @Test
    void create_인원이_정원을_초과하면_400() {
        when(guesthouseRepository.findById(100L)).thenReturn(Optional.of(guesthouse(100L, 10_000, 2)));

        assertThatThrownBy(() -> reservationService.create(1L, request(100L, IN, OUT, 5)))
                .isInstanceOf(IllegalArgumentException.class);

        verify(reservationRepository, never()).save(any());
        verifyNoInteractions(acornLedger);
    }

    @Test
    void create_기존_예약과_기간이_겹쳐도_잔여_정원이_충분하면_예약한다() {
        when(guesthouseRepository.findById(100L)).thenReturn(Optional.of(guesthouse(100L, 10_000, 4)));
        when(dailyOccupancyRepository.findMaxOccupancyByGuesthouseAndDateRange(100L, IN, OUT))
                .thenReturn(2);
//        when(reservationRepository.existsByGuesthouseIdAndStartDateLessThanAndEndDateGreaterThan(eq(100L), any(), any()))
//                .thenReturn(true);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 50L);
            return r;
        });

        Long id = reservationService.create(1L, request(100L, IN, OUT, 2));

        assertThat(id).isEqualTo(50L);
        verify(acornLedger).spend(1L, 30_000, "RESERVATION");
        verify(dailyOccupancyRepository).increaseOccupancy(100L, IN, OUT, 2);
    }

    @Test
    void create_기존_예약으로_잔여_정원을_초과하면_400() {
        when(guesthouseRepository.findById(100L)).thenReturn(Optional.of(guesthouse(100L, 10_000, 5)));
        when(dailyOccupancyRepository.findMaxOccupancyByGuesthouseAndDateRange(100L, IN, OUT))
                .thenReturn(3);

        assertThatThrownBy(() -> reservationService.create(1L, request(100L, IN, OUT, 3)))
                .isInstanceOf(IllegalArgumentException.class);

        verify(reservationRepository, never()).save(any());
        verify(dailyOccupancyRepository, never()).increaseOccupancy(100L, IN, OUT, 3);
        verifyNoInteractions(acornLedger);
    }

    @Test
    void create_요청_인원이_잔여_정원과_같으면_예약한다() {
        when(guesthouseRepository.findById(100L)).thenReturn(Optional.of(guesthouse(100L, 10_000, 5)));
        when(dailyOccupancyRepository.findMaxOccupancyByGuesthouseAndDateRange(100L, IN, OUT))
                .thenReturn(3);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 50L);
            return r;
        });

        Long id = reservationService.create(1L, request(100L, IN, OUT, 2));

        assertThat(id).isEqualTo(50L);
        verify(acornLedger).spend(1L, 30_000, "RESERVATION");
        verify(dailyOccupancyRepository).increaseOccupancy(100L, IN, OUT, 2);
    }

    @Test
    void create_예약하면_숙소_방문자수가_예약인원만큼_증가한다() {
        Guesthouse guesthouse = guesthouse(100L, 10_000, 4);
        guesthouse.setVisitorCount(10);
        when(guesthouseRepository.findById(100L)).thenReturn(Optional.of(guesthouse));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 50L);
            return r;
        });

        reservationService.create(1L, request(100L, IN, OUT, 3));

        assertThat(guesthouse.getVisitorCount()).isEqualTo(13);
    }

    @Test
    void create_없는_숙소면_404() {
        when(guesthouseRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.create(1L, request(100L, IN, OUT, 2)))
                .isInstanceOf(NotFoundException.class);

        verify(reservationRepository, never()).save(any());
        verifyNoInteractions(acornLedger);
    }

    // ---- delete ----

    @Test
    void delete_본인_예약이면_삭제하고_도토리를_전액_환불한다() {
        Reservation r = reservation(10L, 1L, 100L);
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(r));
        when(dailyOccupancyRepository.decreaseOccupancy(100L, IN, OUT, 2)).thenReturn(3);

        reservationService.delete(10L, 1L);

        verify(reservationRepository).delete(r);
        verify(acornLedger).earn(1L, 30_000, "RESERVATION_CANCEL");   // r 의 cost
    }

    @Test
    void delete_예약을_취소하면_점유_인원이_복구되어_겹치는_기간에_다시_예약할_수_있다() {
        LocalDate firstIn = IN;
        LocalDate firstOut = IN.plusDays(2);
        LocalDate secondIn = IN.plusDays(1);
        LocalDate secondOut = IN.plusDays(3);
        Guesthouse guesthouse = guesthouse(100L, 10_000, 4);
        Reservation firstReservation = Reservation.builder()
                .userId(1L).guesthouse(guesthouse)
                .startDate(firstIn).endDate(firstOut).headcount(3).cost(20_000)
                .build();
        ReflectionTestUtils.setField(firstReservation, "id", 50L);

        when(guesthouseRepository.findById(100L)).thenReturn(Optional.of(guesthouse));
        when(dailyOccupancyRepository.findMaxOccupancyByGuesthouseAndDateRange(100L, firstIn, firstOut))
                .thenReturn(0);
        when(dailyOccupancyRepository.findMaxOccupancyByGuesthouseAndDateRange(100L, secondIn, secondOut))
                .thenReturn(0);
        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(firstReservation)
                .thenAnswer(inv -> {
                    Reservation r = inv.getArgument(0);
                    ReflectionTestUtils.setField(r, "id", 51L);
                    return r;
                });
        when(reservationRepository.findById(50L)).thenReturn(Optional.of(firstReservation));
        when(dailyOccupancyRepository.decreaseOccupancy(100L, firstIn, firstOut, 3)).thenReturn(2);

        Long firstReservationId = reservationService.create(
                1L, request(100L, firstIn, firstOut, 3));
        reservationService.delete(firstReservationId, 1L);
        Long secondReservationId = reservationService.create(
                2L, request(100L, secondIn, secondOut, 2));

        assertThat(secondReservationId).isEqualTo(51L);
        verify(dailyOccupancyRepository).decreaseOccupancy(100L, firstIn, firstOut, 3);
        verify(dailyOccupancyRepository).increaseOccupancy(100L, secondIn, secondOut, 2);
    }

    @Test
    void delete_미래_예약을_취소하면_숙소_방문자수가_예약인원만큼_감소한다() {
        Guesthouse guesthouse = guesthouse(100L, 10_000, 4);
        guesthouse.setVisitorCount(10);
        Reservation reservation = Reservation.builder()
                .userId(1L).guesthouse(guesthouse)
                .startDate(IN).endDate(OUT).headcount(3).cost(30_000)
                .build();
        ReflectionTestUtils.setField(reservation, "id", 10L);
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        when(dailyOccupancyRepository.decreaseOccupancy(100L, IN, OUT, 3)).thenReturn(3);

        reservationService.delete(10L, 1L);

        assertThat(guesthouse.getVisitorCount()).isEqualTo(7);
    }

    @Test
    void delete_체크인_당일_예약이면_400이고_취소하지_않는다() {
        Guesthouse guesthouse = guesthouse(100L, 10_000, 4);
        Reservation reservation = Reservation.builder()
                .userId(1L).guesthouse(guesthouse)
                .startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(1))
                .headcount(2).cost(10_000)
                .build();
        ReflectionTestUtils.setField(reservation, "id", 10L);
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.delete(10L, 1L))
                .isInstanceOf(IllegalArgumentException.class);

        verify(reservationRepository, never()).delete(any());
        verifyNoInteractions(dailyOccupancyRepository, acornLedger);
    }

    @Test
    void delete_점유가_숙박일_전체에서_감소하지_않으면_취소하지_않는다() {
        Reservation reservation = reservation(10L, 1L, 100L);
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        when(dailyOccupancyRepository.decreaseOccupancy(100L, IN, OUT, 2)).thenReturn(2);

        assertThatThrownBy(() -> reservationService.delete(10L, 1L))
                .isInstanceOf(IllegalStateException.class);

        verify(reservationRepository, never()).delete(any());
        verifyNoInteractions(acornLedger);
    }

    @Test
    void delete_남의_예약이면_404이고_삭제나_환불을_하지_않는다() {
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation(10L, 1L, 100L)));

        assertThatThrownBy(() -> reservationService.delete(10L, 2L))
                .isInstanceOf(NotFoundException.class);

        verify(reservationRepository, never()).delete(any());
        verifyNoInteractions(acornLedger);
    }
}
