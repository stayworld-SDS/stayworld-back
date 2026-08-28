package com.stayworld.back.reservation.service;

import com.stayworld.back.global.exception.NotFoundException;
import com.stayworld.back.guesthouse.entity.Guesthouse;
import com.stayworld.back.guesthouse.repository.GuesthouseRepository;
import com.stayworld.back.reservation.dto.ReservationCreateRequest;
import com.stayworld.back.reservation.dto.ReservationDetailResponse;
import com.stayworld.back.reservation.dto.ReservationSummaryResponse;
import com.stayworld.back.reservation.entity.Reservation;
import com.stayworld.back.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    // ⚠️ guesthouse 도메인 껍데기에 의존. 실제 구현 나오면 그대로 재사용 가능.
    private final GuesthouseRepository guesthouseRepository;

    /** 유저의 유효한 예약 목록 (체크아웃이 오늘 이후인 건). */
    public List<ReservationSummaryResponse> getMyReservations(Long userId) {
        return reservationRepository
                .findByUserIdAndEndDateGreaterThanEqualOrderByStartDateAsc(userId, LocalDate.now())
                .stream()
                .map(ReservationSummaryResponse::from)
                .toList();
    }

    /** 예약 상세. 없거나 남의 예약이면 404 (존재 여부를 숨긴다). */
    public ReservationDetailResponse getReservation(Long reservationId, Long currentUserId) {
        Reservation reservation = findOwnedReservation(reservationId, currentUserId);
        return ReservationDetailResponse.from(reservation);
    }

    @Transactional
    public Long create(Long userId, ReservationCreateRequest req) {
        Guesthouse guesthouse = guesthouseRepository.findById(req.guesthouseId())
                .orElseThrow(() -> new NotFoundException("숙소를 찾을 수 없습니다."));

        validateDates(req.startDate(), req.endDate());
        validateHeadcount(req.headcount(), guesthouse.getCapacity());
        validateNoOverlap(guesthouse.getId(), req.startDate(), req.endDate());

        long nights = ChronoUnit.DAYS.between(req.startDate(), req.endDate());
        int cost = guesthouse.getPrice() * (int) nights;

        Reservation reservation = Reservation.builder()
                .userId(userId)
                .guesthouse(guesthouse)
                .startDate(req.startDate())
                .endDate(req.endDate())
                .headcount(req.headcount())
                .cost(cost)
                .build();

        return reservationRepository.save(reservation).getId();
    }

    @Transactional
    public void delete(Long reservationId, Long currentUserId) {
        Reservation reservation = findOwnedReservation(reservationId, currentUserId);
        reservationRepository.delete(reservation);
    }

    // --- helpers ---

    private Reservation findOwnedReservation(Long reservationId, Long currentUserId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다."));
        if (!reservation.isOwnedBy(currentUserId)) {
            throw new NotFoundException("예약을 찾을 수 없습니다.");
        }
        return reservation;
    }

    // 단일 필드 검증(필수값, 지난 날짜, 최소 인원)은 ReservationCreateRequest 의 Bean Validation 이 담당.
    // 여기서는 교차 검증만.
    private void validateDates(LocalDate start, LocalDate end) {
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("체크아웃은 체크인보다 뒤여야 합니다.");
        }
    }

    private void validateHeadcount(int headcount, int capacity) {
        if (headcount > capacity) {
            throw new IllegalArgumentException("숙소 수용 인원(" + capacity + "명)을 초과했습니다.");
        }
    }

    private void validateNoOverlap(Long guesthouseId, LocalDate start, LocalDate end) {
        boolean overlaps = reservationRepository
                .existsByGuesthouse_IdAndStartDateLessThanAndEndDateGreaterThan(guesthouseId, end, start);
        if (overlaps) {
            throw new IllegalArgumentException("해당 기간에 이미 예약이 있습니다.");
        }
    }
}
