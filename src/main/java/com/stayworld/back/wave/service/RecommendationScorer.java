package com.stayworld.back.wave.service;

import com.stayworld.back.wave.dto.Reason;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 후보들을 점수 매겨 상위 N명으로 자른다. 순수 로직(DB 무관) — 가중치 튜닝은 여기 한 곳에서만.
 *
 * <p>점수 = 가중합. 각 신호는 상한(cap)을 둬서 한 신호가 과대평가되는 걸 막는다.
 * 마지막에 작은 랜덤 지터를 더해 동점을 흩고 매번 조금씩 다른 피드를 준다
 * (지터 최대치 {@value #W_RANDOM} 는 신호 1점 차이를 못 뒤집도록 1.0 보다 작게 잡음).
 */
@Component
public class RecommendationScorer {

    static final int W_COVISIT = 5;
    static final int W_FOF = 3;
    static final int W_COREGION = 1;
    static final double W_RANDOM = 0.5;

    static final int CAP_COVISIT = 5;
    static final int CAP_FOF = 5;
    static final int CAP_COREGION = 3;

    List<ScoredCandidate> rank(Collection<CandidateAccumulator> candidates, int limit) {
        return candidates.stream()
                .map(this::score)
                .sorted(Comparator.comparingDouble(ScoredCandidate::score).reversed())
                .limit(Math.max(limit, 0))
                .toList();
    }

    private ScoredCandidate score(CandidateAccumulator a) {
        int covisit = Math.min(a.sharedGuesthouseCount, CAP_COVISIT);
        int fof = Math.min(a.mutualFriendIds.size(), CAP_FOF);
        int coregion = Math.min(a.sharedRegionCount, CAP_COREGION);

        double score = W_COVISIT * covisit
                + W_FOF * fof
                + W_COREGION * coregion
                + W_RANDOM * ThreadLocalRandom.current().nextDouble();

        return new ScoredCandidate(a.userId, score, buildReasons(a));
    }

    /** 강한 신호부터. 신호가 하나도 없으면 랜덤 사유 하나. */
    private List<Reason> buildReasons(CandidateAccumulator a) {
        List<Reason> reasons = new ArrayList<>();
        if (a.sharedGuesthouseCount > 0) {
            reasons.add(new Reason("CO_VISITOR",
                    "게스트하우스 " + a.sharedGuesthouseCount + "곳을 함께 다녀왔어요"));
        }
        if (!a.mutualFriendIds.isEmpty()) {
            reasons.add(new Reason("MUTUAL_FRIEND",
                    "공통 일촌 " + a.mutualFriendIds.size() + "명"));
        }
        if (a.sharedRegionCount > 0) {
            reasons.add(new Reason("CO_REGION",
                    "여행 지역이 " + a.sharedRegionCount + "곳 겹쳐요"));
        }
        if (reasons.isEmpty()) {
            reasons.add(new Reason("RANDOM", "오늘의 새로운 인연"));
        }
        return reasons;
    }
}
