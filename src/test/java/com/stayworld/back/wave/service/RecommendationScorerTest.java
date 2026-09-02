package com.stayworld.back.wave.service;

import com.stayworld.back.wave.dto.Reason;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationScorerTest {

    private final RecommendationScorer scorer = new RecommendationScorer();

    @Test
    void 같은_게하_공통일촌_같은지역_순으로_랭크된다() {
        CandidateAccumulator coVisitor = acc(1L);
        coVisitor.sharedGuesthouseCount = 1;                 // 5 + 지터
        CandidateAccumulator fof = acc(2L);
        fof.mutualFriendIds.add(99L);                        // 3 + 지터
        CandidateAccumulator coRegion = acc(3L);
        coRegion.sharedRegionCount = 1;                      // 1 + 지터

        List<ScoredCandidate> ranked = scorer.rank(List.of(coRegion, fof, coVisitor), 10);

        assertThat(ranked).extracting(ScoredCandidate::userId).containsExactly(1L, 2L, 3L);
    }

    @Test
    void limit_만큼만_돌려준다() {
        List<CandidateAccumulator> many = LongStream.rangeClosed(1, 10)
                .mapToObj(id -> {
                    CandidateAccumulator a = acc(id);
                    a.fromRandom = true;
                    return a;
                })
                .toList();

        assertThat(scorer.rank(many, 3)).hasSize(3);
    }

    @Test
    void 사유가_강한_신호_순서로_붙는다() {
        CandidateAccumulator a = acc(1L);
        a.sharedGuesthouseCount = 2;
        a.mutualFriendIds.addAll(List.of(10L, 11L, 12L));
        a.sharedRegionCount = 1;

        List<Reason> reasons = scorer.rank(List.of(a), 1).get(0).reasons();

        assertThat(reasons).extracting(Reason::type)
                .containsExactly("CO_VISITOR", "MUTUAL_FRIEND", "CO_REGION");
        assertThat(reasons.get(0).label()).contains("2곳");
        assertThat(reasons.get(1).label()).contains("3명");
    }

    @Test
    void 신호가_없으면_랜덤_사유_하나만() {
        CandidateAccumulator a = acc(1L);
        a.fromRandom = true;

        List<Reason> reasons = scorer.rank(List.of(a), 1).get(0).reasons();

        assertThat(reasons).extracting(Reason::type).containsExactly("RANDOM");
    }

    @Test
    void 신호_상한이_적용된다() {
        CandidateAccumulator capped = acc(1L);
        capped.sharedGuesthouseCount = 100;   // CAP_COVISIT(5) 로 클램프
        CandidateAccumulator exact = acc(2L);
        exact.sharedGuesthouseCount = 5;

        List<ScoredCandidate> ranked = scorer.rank(List.of(capped, exact), 2);

        assertThat(ranked.get(0).score()).isLessThan(26.0);   // 5*5 + 지터(<0.5)
        assertThat(Math.abs(ranked.get(0).score() - ranked.get(1).score())).isLessThan(0.5);
    }

    private static CandidateAccumulator acc(long id) {
        return new CandidateAccumulator(id);
    }
}
