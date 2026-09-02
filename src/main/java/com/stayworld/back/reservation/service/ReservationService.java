package com.stayworld.back.reservation.service;

import com.stayworld.back.acorn.service.AcornLedger;
import com.stayworld.back.global.exception.NotFoundException;
import com.stayworld.back.guesthouse.entity.Guesthouse;
import com.stayworld.back.guesthouse.exception.GuesthouseNotFoundException;
import com.stayworld.back.guesthouse.repository.GuesthouseRepository;
import com.stayworld.back.reservation.dto.ReservationCreateRequest;
import com.stayworld.back.reservation.dto.ReservationDetailResponse;
import com.stayworld.back.reservation.dto.ReservationSummaryResponse;
import com.stayworld.back.reservation.entity.Reservation;
import com.stayworld.back.reservation.repository.DailyOccupancyRepository;
import com.stayworld.back.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static final String REASON_PAYMENT = "RESERVATION";
    private static final String REASON_CANCEL = "RESERVATION_CANCEL";

    private final ReservationRepository reservationRepository;
    private final DailyOccupancyRepository dailyOccupancyRepository;
    private final GuesthouseRepository guesthouseRepository;
    private final AcornLedger acornLedger;

    /** 유저의 유효한 예약 목록 (체크아웃이 오늘 이후인 건, 체크인 빠른 순). */
    public List<ReservationSummaryResponse> getMyReservations(Long userId) {
        return toSummaries(reservationRepository
                .findByUserIdAndEndDateGreaterThanEqualOrderByStartDateAsc(userId, LocalDate.now()));
    }

    /** 유저가 다녀온 예약 목록 (체크아웃이 오늘 이전인 건, 최근 순). 취소한 예약은 하드 삭제라 여기 안 나온다. */
    public List<ReservationSummaryResponse> getReservationHistory(Long userId) {
        return toSummaries(reservationRepository
                .findByUserIdAndEndDateLessThanOrderByStartDateDesc(userId, LocalDate.now()));
    }

    private List<ReservationSummaryResponse> toSummaries(List<Reservation> reservations) {

        return reservations.stream()
                .map(r -> ReservationSummaryResponse.from(r, r.getGuesthouse()))
                .toList();
    }

    /** 예약 상세. 없거나 남의 예약이면 404 (존재 여부를 숨긴다). */
    public ReservationDetailResponse getReservation(Long reservationId, Long currentUserId) {
        Reservation reservation = findOwnedReservation(reservationId, currentUserId);
        return ReservationDetailResponse.from(reservation, reservation.getGuesthouse());
    }

    @Transactional
    public Long create(Long userId, ReservationCreateRequest req) {
        Guesthouse guesthouse = guesthouseRepository.findById(req.getGuesthouseId())
                .orElseThrow(GuesthouseNotFoundException::new); // 없으면 GuesthouseNotFoundException

        validateDates(req.getStartDate(), req.getEndDate());
        validateHeadcount(req.getHeadcount(), req.getStartDate(), req.getEndDate(), guesthouse);

        long nights = ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate());
        int cost = guesthouse.getPrice() * (int) nights * req.getHeadcount();

        // 잔액 부족이면 InsufficientAcornException(400) → 트랜잭션 롤백, 예약 통째로 실패
        acornLedger.spend(userId, cost, REASON_PAYMENT);

        // 방문자수 증가
        guesthouse.setVisitorCount(guesthouse.getVisitorCount() + req.getHeadcount());

        Reservation reservation = Reservation.builder()
                .userId(userId)
                .guesthouse(guesthouse)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .headcount(req.getHeadcount())
                .cost(cost)
                .build();

        dailyOccupancyRepository.increaseOccupancy(guesthouse.getId(), req.getStartDate(), req.getEndDate(), req.getHeadcount());
        return reservationRepository.save(reservation).getId();
    }

    /** 예약 취소. 결제한 도토리를 전액 환불한다. */
    @Transactional
    public void delete(Long reservationId, Long currentUserId) {
        Reservation reservation = findOwnedReservation(reservationId, currentUserId);
        if (!reservation.getStartDate().isAfter(LocalDate.now(KST))) {
            throw new IllegalArgumentException("체크인 전 예약만 취소할 수 있습니다.");
        }

        Guesthouse guesthouse = reservation.getGuesthouse();
        if (guesthouse.getVisitorCount() < reservation.getHeadcount()) {
            throw new IllegalStateException("숙소 방문자 수가 예약 인원보다 적습니다.");
        }

        int updatedDays = dailyOccupancyRepository.decreaseOccupancy(
                guesthouse.getId(), reservation.getStartDate(), reservation.getEndDate(), reservation.getHeadcount()
        );
        int nights = (int) ChronoUnit.DAYS.between(reservation.getStartDate(), reservation.getEndDate());
        if (updatedDays != nights) {
            throw new IllegalStateException("예약 기간의 점유 인원을 복구하지 못했습니다.");
        }

        guesthouse.setVisitorCount(guesthouse.getVisitorCount() - reservation.getHeadcount());
        guesthouseRepository.save(guesthouse);
        reservationRepository.delete(reservation);

        acornLedger.earn(currentUserId, reservation.getCost(), REASON_CANCEL);
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

    private void validateHeadcount(int headcount, LocalDate startDate, LocalDate endDate, Guesthouse guesthouse) {

        int maxOccupancy = dailyOccupancyRepository.findMaxOccupancyByGuesthouseAndDateRange(guesthouse.getId(), startDate, endDate);
        int availability = guesthouse.getCapacity() - maxOccupancy;
        if (headcount > availability) {
            throw new IllegalArgumentException("수용 가능 인원(" + availability + "명)을 초과했습니다.");
        }
    }
}
