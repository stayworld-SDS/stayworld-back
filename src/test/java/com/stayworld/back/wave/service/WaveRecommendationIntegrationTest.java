package com.stayworld.back.wave.service;

import com.stayworld.back.friend.entity.Friend;
import com.stayworld.back.friend.repository.FriendRepository;
import com.stayworld.back.friend.service.KinshipCalculator;
import com.stayworld.back.guesthouse.entity.Guesthouse;
import com.stayworld.back.guesthouse.repository.GuesthouseRepository;
import com.stayworld.back.reservation.entity.Reservation;
import com.stayworld.back.reservation.repository.ReservationRepository;
import com.stayworld.back.user.entity.User;
import com.stayworld.back.user.repository.UserRepository;
import com.stayworld.back.wave.dto.RecommendationResponse;
import com.stayworld.back.wave.dto.RecommendedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 100명 규모로 촌수/추천이 실제 DB(H2) 위에서 정상 동작하는지 + 대략의 응답 시간을 확인한다.
 * 정확성 단언은 {@link KinshipCalculatorTest}/{@link RecommendationScorerTest} 가 담당하고,
 * 여기서는 "쿼리가 H2에서 깨지지 않는가 + 결과가 상식적인가 + 느리지 않은가" 를 본다.
 */
@DataJpaTest
@Import({KinshipCalculator.class, RecommendationScorer.class, RecommendationService.class})
class WaveRecommendationIntegrationTest {

    private static final int USER_COUNT = 100;
    private static final String[] REGIONS_ADDR = {
            "제주시 애월읍", "강원도 평창군", "서울특별시 마포구", "부산광역시 해운대구"
    };

    @Autowired UserRepository userRepository;
    @Autowired FriendRepository friendRepository;
    @Autowired ReservationRepository reservationRepository;
    @Autowired GuesthouseRepository guesthouseRepository;
    @Autowired KinshipCalculator kinshipCalculator;
    @Autowired RecommendationService recommendationService;

    private final Random rnd = new Random(42);
    private List<Long> userIds;
    private final Set<String> friendEdges = new HashSet<>();

    @BeforeEach
    void seed() {
        userIds = new ArrayList<>();
        for (int i = 0; i < USER_COUNT; i++) {
            User u = new User();
            u.setEmail("user" + i + "@test.com");
            u.setPassword("x");
            u.setNickname("유저" + i);
            u.setPhoneNumber("010-0000-" + String.format("%04d", i));
            u.setBalance(50_000);
            u.setVisitorCount(0);
            u.setCreatedAt(LocalDateTime.now().minusDays(i));
            userIds.add(userRepository.save(u).getId());
        }

        // 친구 그래프: 링(i-i+1) + 각자 랜덤 코드 3개. 단방향 1행으로만 저장(무방향 처리 검증 겸).
        for (int i = 0; i < USER_COUNT; i++) {
            linkFriend(userIds.get(i), userIds.get((i + 1) % USER_COUNT));
            for (int k = 0; k < 3; k++) {
                long other = userIds.get(rnd.nextInt(USER_COUNT));
                if (other != userIds.get(i)) {
                    linkFriend(userIds.get(i), other);
                }
            }
        }

        // 게하 12곳, 4개 지역에 3곳씩. region 은 @PrePersist 가 주소에서 채운다.
        List<Guesthouse> houses = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Guesthouse g = new Guesthouse();
            g.setName("게하" + i);
            g.setPrice(50_000);
            g.setPhoneNumber("064-000-0000");
            g.setAddress(REGIONS_ADDR[i % REGIONS_ADDR.length] + " " + i + "길 " + (i + 1));
            g.setCapacity(8);
            g.setParkingProvided(true);
            g.setWifiProvided(true);
            g.setBreakfastProvided(false);
            g.setVisitorCount(0);
            houses.add(guesthouseRepository.save(g));
        }

        // 예약: 각 유저 1~4건, 모두 '다녀온'(endDate 과거).
        for (Long uid : userIds) {
            int count = 1 + rnd.nextInt(4);
            for (int k = 0; k < count; k++) {
                Guesthouse g = houses.get(rnd.nextInt(houses.size()));
                LocalDate end = LocalDate.now().minusDays(1 + rnd.nextInt(60));
                reservationRepository.save(Reservation.builder()
                        .userId(uid)
                        .guesthouse(g)
                        .startDate(end.minusDays(2))
                        .endDate(end)
                        .headcount(2)
                        .cost(100_000)
                        .build());
            }
        }
        userRepository.flush();
        friendRepository.flush();
        reservationRepository.flush();
    }

    private void linkFriend(long a, long b) {
        if (a == b || !friendEdges.add(a + ":" + b)) {
            return;   // (user_id, friend_id) 유니크 제약 회피
        }
        friendRepository.save(Friend.builder().userId(a).friendId(b).build());
    }

    @Test
    void 촌수_계산이_정상이고_빠르다() {
        long me = userIds.get(0);
        long ringNeighbor = userIds.get(1);

        assertThat(kinshipCalculator.shortestPath(me, me)).contains(List.of(me));
        assertThat(kinshipCalculator.shortestPath(me, ringNeighbor))
                .hasValueSatisfying(path -> assertThat(path).hasSize(2));      // 1촌
        assertThat(kinshipCalculator.shortestPath(me, userIds.get(2)))
                .hasValueSatisfying(path -> assertThat(path.size()).isBetween(2, 3)); // 1~2촌

        int reachable = 0;
        long start = System.nanoTime();
        int pairs = 300;
        for (int i = 0; i < pairs; i++) {
            long a = userIds.get(rnd.nextInt(USER_COUNT));
            long b = userIds.get(rnd.nextInt(USER_COUNT));
            Optional<List<Long>> path = kinshipCalculator.shortestPath(a, b);
            if (path.isPresent()) {
                reachable++;
                assertThat(path.get()).first().isEqualTo(a);
                assertThat(path.get()).last().isEqualTo(b);
            }
        }
        double avgMs = (System.nanoTime() - start) / 1_000_000.0 / pairs;

        System.out.printf("[촌수] %d쌍, 평균 %.2f ms/쌍, 6촌내 도달 %d/%d%n", pairs, avgMs, reachable, pairs);
        assertThat(avgMs).isLessThan(50.0);
    }

    @Test
    void 추천이_상식적인_결과를_상식적인_시간에_돌려준다() {
        long me = userIds.get(7);
        Set<Long> myFriends = directFriendsOf(me);

        long start = System.nanoTime();
        RecommendationResponse res = recommendationService.recommend(me, 10, true);
        double firstMs = (System.nanoTime() - start) / 1_000_000.0;

        List<RecommendedUser> cards = res.recommendations();
        assertThat(cards).isNotEmpty().hasSizeLessThanOrEqualTo(10);
        assertThat(cards).allSatisfy(c -> {
            assertThat(c.userId()).isNotEqualTo(me);
            assertThat(myFriends).doesNotContain(c.userId());   // 이미 일촌은 제외
            assertThat(c.reasons()).isNotEmpty();
        });
        // 공통 일촌 사유가 붙은 카드는 정확히 2촌
        assertThat(cards).filteredOn(c -> c.reasons().stream()
                        .anyMatch(r -> r.type().equals("MUTUAL_FRIEND")))
                .allSatisfy(c -> assertThat(c.degreeFromMe()).isEqualTo(2));

        int runs = 30;
        start = System.nanoTime();
        for (int i = 0; i < runs; i++) {
            recommendationService.recommend(userIds.get(rnd.nextInt(USER_COUNT)), 10, false);
        }
        double avgMs = (System.nanoTime() - start) / 1_000_000.0 / runs;

        System.out.printf("[추천] 첫 호출 %.1f ms, 이후 평균 %.1f ms (%d회, withDegree=false)%n", firstMs, avgMs, runs);
        System.out.println("[추천] me=" + me + " 결과:");
        cards.forEach(c -> System.out.printf("  - %s (id=%d, 촌수=%s, 방문객=%d) %s%n",
                c.nickname(), c.userId(), c.degreeFromMe(), c.visitorCount(),
                c.reasons().stream().map(r -> r.label()).toList()));

        assertThat(avgMs).isLessThan(300.0);
    }

    @Test
    void 신규유저는_콜드스타트로_랜덤_추천만_받는다() {
        User fresh = new User();
        fresh.setEmail("fresh@test.com");
        fresh.setPassword("x");
        fresh.setNickname("뉴비");
        fresh.setPhoneNumber("010-9999-9999");
        fresh.setBalance(50_000);
        fresh.setVisitorCount(0);
        fresh.setCreatedAt(LocalDateTime.now());
        long freshId = userRepository.save(fresh).getId();

        RecommendationResponse res = recommendationService.recommend(freshId, 10, false);

        assertThat(res.recommendations()).hasSize(10);
        assertThat(res.recommendations()).allSatisfy(c -> {
            assertThat(c.userId()).isNotEqualTo(freshId);
            assertThat(c.degreeFromMe()).isNull();
            assertThat(c.reasons()).singleElement()
                    .satisfies(r -> assertThat(r.type()).isEqualTo("RANDOM"));
        });
    }

    private Set<Long> directFriendsOf(long me) {
        Set<Long> ids = new HashSet<>();
        friendRepository.findByUserIdIn(List.of(me)).forEach(f -> ids.add(f.getFriendId()));
        friendRepository.findByFriendIdIn(List.of(me)).forEach(f -> ids.add(f.getUserId()));
        return ids;
    }
}
