package com.stayworld.back.profile.service;

import com.stayworld.back.global.exception.NotFoundException;
import com.stayworld.back.profile.dto.FootprintDto;
import com.stayworld.back.profile.dto.VisitResponse;
import com.stayworld.back.profile.entity.ProfileVisit;
import com.stayworld.back.profile.repository.ProfileVisitRepository;
import com.stayworld.back.user.entity.User;
import com.stayworld.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 미니홈피 방문 이벤트의 단일 창구. 파도타기·검색·직접 링크 등 모든 진입 경로가 여기로 모여
 * 투데이(방문자 수)를 올리고 발자국을 남긴다.
 *
 * <p>본인 방문은 세지 않고, 같은 사람이 같은 날 다시 오면 한 번만 센다.
 *
 * <p>TODO(동시성): {@code exists} 체크 후 {@code visitor_count} 를 load-modify-save 라
 * 같은 홈피에 동시 첫 방문이 겹치면 카운트가 유실될 수 있다 ({@code AcornLedger} 와 동일, {@code TODO.md}).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileVisitService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int FOOTPRINT_LIMIT = 20;

    private final ProfileVisitRepository profileVisitRepository;
    private final UserRepository userRepository;

    @Transactional
    public VisitResponse recordVisit(Long ownerId, Long visitorId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));

        if (ownerId.equals(visitorId)) {
            return new VisitResponse(false, owner.getVisitorCount());
        }

        LocalDate today = LocalDate.now(KST);
        if (profileVisitRepository.existsByOwnerIdAndVisitorIdAndVisitDate(ownerId, visitorId, today)) {
            return new VisitResponse(false, owner.getVisitorCount());
        }

        profileVisitRepository.save(new ProfileVisit(ownerId, visitorId, today));
        owner.setVisitorCount(owner.getVisitorCount() + 1);   // 더티 체킹으로 커밋 시 반영
        return new VisitResponse(true, owner.getVisitorCount());
    }

    /** 방문자별 가장 최근 방문 기준 최근 발자국 (최대 {@value #FOOTPRINT_LIMIT}개). */
    public List<FootprintDto> footprints(long ownerId) {
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException("유저를 찾을 수 없습니다.");
        }

        Map<Long, ProfileVisit> latestByVisitor = new LinkedHashMap<>();
        for (ProfileVisit visit : profileVisitRepository.findTop50ByOwnerIdOrderByIdDesc(ownerId)) {
            latestByVisitor.putIfAbsent(visit.getVisitorId(), visit);
        }

        List<Long> visitorIds = List.copyOf(latestByVisitor.keySet());
        Map<Long, String> nicknameById = userRepository.findAllById(visitorIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));

        return latestByVisitor.values().stream()
                .limit(FOOTPRINT_LIMIT)
                .map(v -> new FootprintDto(v.getVisitorId(), nicknameById.get(v.getVisitorId()), v.getCreatedAt()))
                .toList();
    }
}
