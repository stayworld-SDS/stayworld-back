package com.stayworld.back.wave.service;

import com.stayworld.back.friend.entity.Friend;
import com.stayworld.back.friend.repository.FriendRepository;
import com.stayworld.back.friend.service.KinshipCalculator;
import com.stayworld.back.global.dto.IdCount;
import com.stayworld.back.guesthouse.repository.GuesthouseRepository;
import com.stayworld.back.reservation.repository.ReservationRepository;
import com.stayworld.back.user.entity.User;
import com.stayworld.back.user.repository.UserRepository;
import com.stayworld.back.wave.dto.RecommendationResponse;
import com.stayworld.back.wave.dto.RecommendedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 파도타기 "사람 추천" 피드. 여러 소스에서 후보를 모아(recall) 점수 매겨(rank) 상위 N명을 돌려준다.
 *
 * <p>소스: ① 2촌(친구의 친구, 공통 일촌 수) ② 같은 게하 방문자(공유 게하 수)
 * ③ 같은 지역 방문자(공유 지역 수) ④ 랜덤 탐색(콜드스타트 + 우연성, 항상 일부 포함).
 * 이미 일촌이거나 본인은 제외한다. 부수효과 없음 — 실제 방문/보상은 {@link WaveService}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int MAX_LIMIT = 30;
    private static final int MIN_RANDOM_FILL = 3;

    private final FriendRepository friendRepository;
    private final ReservationRepository reservationRepository;
    private final GuesthouseRepository guesthouseRepository;
    private final UserRepository userRepository;
    private final KinshipCalculator kinshipCalculator;
    private final RecommendationScorer scorer;

    public RecommendationResponse recommend(long me, int limit, boolean withDegree) {
        int cappedLimit = Math.clamp(limit, 1, MAX_LIMIT);
        LocalDate today = LocalDate.now(KST);

        Set<Long> myFriendIds = neighborsOf(me);
        Set<Long> exclude = new HashSet<>(myFriendIds);
        exclude.add(me);

        Map<Long, CandidateAccumulator> candidates = new HashMap<>();
        collectFriendsOfFriends(myFriendIds, exclude, candidates);

        List<Long> myGuesthouseIds = reservationRepository.findDistinctVisitedGuesthouseIds(me, today);
        collectCoVisitors(myGuesthouseIds, me, today, exclude, candidates);
        collectCoRegionVisitors(myGuesthouseIds, me, today, exclude, candidates);

        fillRandom(me, exclude, candidates, cappedLimit);

        List<ScoredCandidate> ranked = scorer.rank(candidates.values(), cappedLimit);
        return new RecommendationResponse(hydrate(me, ranked, candidates, withDegree));
    }

    // --- recall ---

    /** 무방향 그래프에서 내 일촌 id 집합. */
    private Set<Long> neighborsOf(long userId) {
        Set<Long> ids = Set.of(userId);
        Set<Long> neighbors = new HashSet<>();
        for (Friend f : friendRepository.findByUserIdIn(ids)) {
            neighbors.add(f.getFriendId());
        }
        for (Friend f : friendRepository.findByFriendIdIn(ids)) {
            neighbors.add(f.getUserId());
        }
        neighbors.remove(userId);
        return neighbors;
    }

    private void collectFriendsOfFriends(Set<Long> myFriendIds, Set<Long> exclude,
                                         Map<Long, CandidateAccumulator> candidates) {
        if (myFriendIds.isEmpty()) {
            return;
        }
        for (Friend f : friendRepository.findByUserIdIn(myFriendIds)) {
            link(f.getUserId(), f.getFriendId(), exclude, candidates);
        }
        for (Friend f : friendRepository.findByFriendIdIn(myFriendIds)) {
            link(f.getFriendId(), f.getUserId(), exclude, candidates);
        }
    }

    /** {@code viaFriend}(내 일촌)를 통해 {@code candidate} 가 2촌으로 걸린다. */
    private void link(long viaFriend, long candidate, Set<Long> exclude,
                      Map<Long, CandidateAccumulator> candidates) {
        if (exclude.contains(candidate)) {
            return;
        }
        candidates.computeIfAbsent(candidate, CandidateAccumulator::new).mutualFriendIds.add(viaFriend);
    }

    private void collectCoVisitors(List<Long> myGuesthouseIds, long me, LocalDate today,
                                   Set<Long> exclude, Map<Long, CandidateAccumulator> candidates) {
        if (myGuesthouseIds.isEmpty()) {
            return;
        }
        for (IdCount row : reservationRepository.findCoVisitors(myGuesthouseIds, today, me)) {
            if (exclude.contains(row.getId())) {
                continue;
            }
            candidates.computeIfAbsent(row.getId(), CandidateAccumulator::new)
                    .sharedGuesthouseCount = (int) row.getCount();
        }
    }

    private void collectCoRegionVisitors(List<Long> myGuesthouseIds, long me, LocalDate today,
                                         Set<Long> exclude, Map<Long, CandidateAccumulator> candidates) {
        if (myGuesthouseIds.isEmpty()) {
            return;
        }
        List<String> myRegions = guesthouseRepository.findDistinctRegionsByIdIn(myGuesthouseIds).stream()
                .filter(r -> r != null && !r.isBlank())
                .toList();
        if (myRegions.isEmpty()) {
            return;
        }
        for (IdCount row : reservationRepository.findCoRegionVisitors(myRegions, today, me)) {
            if (exclude.contains(row.getId())) {
                continue;
            }
            candidates.computeIfAbsent(row.getId(), CandidateAccumulator::new)
                    .sharedRegionCount = (int) row.getCount();
        }
    }

    /**
     * 신호형 후보가 {@code limit} 에 못 미치면 랜덤으로 채운다 (최소 {@link #MIN_RANDOM_FILL} 명은 항상).
     * offset 한 건씩 뽑는 방식 — 대규모에선 seek 페이지네이션으로 바꿔야 하지만 현재 규모엔 충분.
     */
    private void fillRandom(long me, Set<Long> exclude, Map<Long, CandidateAccumulator> candidates, int limit) {
        int want = Math.max(MIN_RANDOM_FILL, limit - candidates.size());
        long pool = userRepository.count() - 1; // 나 제외
        if (want <= 0 || pool <= 0) {
            return;
        }

        int maxAttempts = want * 4;
        for (int attempts = 0; want > 0 && attempts < maxAttempts; attempts++) {
            int offset = ThreadLocalRandom.current().nextInt((int) Math.min(pool, Integer.MAX_VALUE));
            List<User> picked = userRepository.findRandomPool(me, PageRequest.of(offset, 1));
            if (picked.isEmpty()) {
                break;
            }
            long id = picked.get(0).getId();
            if (exclude.contains(id) || candidates.containsKey(id)) {
                continue;
            }
            CandidateAccumulator acc = new CandidateAccumulator(id);
            acc.fromRandom = true;
            candidates.put(id, acc);
            want--;
        }
    }

    // --- hydrate ---

    private List<RecommendedUser> hydrate(long me, List<ScoredCandidate> ranked,
                                          Map<Long, CandidateAccumulator> candidates, boolean withDegree) {
        if (ranked.isEmpty()) {
            return List.of();
        }
        List<Long> ids = ranked.stream().map(ScoredCandidate::userId).toList();

        Map<Long, User> userById = userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, Long> friendCountById = friendRepository.countByUserIdIn(ids).stream()
                .collect(Collectors.toMap(IdCount::getId, IdCount::getCount));
        Map<Long, Integer> degreeById = withDegree
                ? kinshipCalculator.degrees(me, new HashSet<>(ids))
                : Map.of();

        List<RecommendedUser> result = new ArrayList<>();
        for (ScoredCandidate sc : ranked) {
            User u = userById.get(sc.userId());
            if (u == null) {
                continue; // 추천 계산 도중 탈퇴 등
            }
            result.add(new RecommendedUser(
                    u.getId(),
                    u.getNickname(),
                    u.getVisitorCount(),
                    friendCountById.getOrDefault(u.getId(), 0L),
                    u.getCreatedAt(),
                    resolveDegree(sc.userId(), candidates.get(sc.userId()), degreeById, withDegree),
                    sc.reasons()));
        }
        return result;
    }

    private Integer resolveDegree(long candidateId, CandidateAccumulator acc,
                                  Map<Long, Integer> degreeById, boolean withDegree) {
        if (withDegree) {
            return degreeById.get(candidateId);
        }
        // 배치 BFS 없이도 2촌은 확실: 내 일촌은 exclude 됐으니 공통 일촌이 있으면 정확히 2촌.
        return acc != null && acc.isFriendOfFriend() ? 2 : null;
    }
}
