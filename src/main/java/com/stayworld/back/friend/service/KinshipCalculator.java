package com.stayworld.back.friend.service;

import com.stayworld.back.friend.entity.Friend;
import com.stayworld.back.friend.repository.FriendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 일촌 그래프에서 촌수(최단거리)를 계산한다. 그래프는 <b>무방향</b>으로 취급한다
 * ({@code friends} 는 한 방향 1행이라, 양쪽 방향 행 중 하나라도 있으면 간선으로 본다).
 *
 * <p>레벨 동기 BFS: 레벨마다 프론티어 전체의 이웃을 쿼리 2번으로 가져온다.
 * 사회 그래프는 지름이 작아 대개 몇 레벨 안에 끝나고, 최악에도 쿼리는 레벨 수만큼만 나간다.
 * {@link #MAX_DEPTH} 밖은 "남"으로, 방문 노드가 {@link #MAX_VISITED} 를 넘으면 계산을 포기한다
 * (취미 규모 데이터에선 후자는 발동하지 않는다).
 */
@Component
@RequiredArgsConstructor
public class KinshipCalculator {

    /** 이 깊이 안에 닿지 못하면 "관계 없음"으로 본다 (케빈 베이컨 6단계). */
    static final int MAX_DEPTH = 6;

    /** 관계망이 이보다 커지면 계산 포기 (폭주 가드). */
    static final int MAX_VISITED = 10_000;

    private final FriendRepository friendRepository;

    /**
     * {@code src} → {@code dst} 최단 경로. 반환 리스트는 {@code [src, ..., dst]} 이고
     * 촌수는 {@code size() - 1}. {@link #MAX_DEPTH} 안에 도달 불가하거나 관계망이
     * {@link #MAX_VISITED} 를 넘으면 {@link Optional#empty()}.
     */
    public Optional<List<Long>> shortestPath(long src, long dst) {
        if (src == dst) {
            return Optional.of(List.of(src));
        }

        Map<Long, Long> parent = new HashMap<>();   // 노드 -> 이 노드를 처음 발견한 노드
        parent.put(src, null);
        Set<Long> frontier = Set.of(src);

        for (int depth = 1; depth <= MAX_DEPTH && !frontier.isEmpty(); depth++) {
            Set<Long> next = new HashSet<>();
            for (long[] edge : neighborEdges(frontier)) {
                long to = edge[1];
                if (parent.containsKey(to)) {
                    continue;
                }
                parent.put(to, edge[0]);
                if (to == dst) {
                    return Optional.of(rebuildPath(parent, dst));
                }
                next.add(to);
            }
            if (parent.size() > MAX_VISITED) {
                return Optional.empty();
            }
            frontier = next;
        }
        return Optional.empty();
    }

    /**
     * {@code src} 로부터 여러 대상까지의 촌수를 BFS 한 번으로 계산한다 (추천 카드의 촌수 배지용).
     * {@link #MAX_DEPTH} 안에 닿지 못한 대상은 결과 맵에서 빠진다.
     */
    public Map<Long, Integer> degrees(long src, Set<Long> targets) {
        Map<Long, Integer> result = new HashMap<>();
        Set<Long> remaining = new HashSet<>(targets);
        if (remaining.remove(src)) {
            result.put(src, 0);
        }
        if (remaining.isEmpty()) {
            return result;
        }

        Set<Long> visited = new HashSet<>();
        visited.add(src);
        Set<Long> frontier = Set.of(src);

        for (int depth = 1; depth <= MAX_DEPTH && !frontier.isEmpty() && !remaining.isEmpty(); depth++) {
            Set<Long> next = new HashSet<>();
            for (long[] edge : neighborEdges(frontier)) {
                long to = edge[1];
                if (!visited.add(to)) {
                    continue;
                }
                if (remaining.remove(to)) {
                    result.put(to, depth);
                }
                next.add(to);
            }
            if (visited.size() > MAX_VISITED) {
                break;
            }
            frontier = next;
        }
        return result;
    }

    /** 프론티어의 모든 이웃 간선을 {@code (from, to)} 쌍으로. 방향은 {@code from} → {@code to} 로 통일. */
    private List<long[]> neighborEdges(Set<Long> ids) {
        List<long[]> edges = new ArrayList<>();
        for (Friend f : friendRepository.findByUserIdIn(ids)) {
            edges.add(new long[]{f.getUserId(), f.getFriendId()});
        }
        for (Friend f : friendRepository.findByFriendIdIn(ids)) {
            edges.add(new long[]{f.getFriendId(), f.getUserId()});
        }
        return edges;
    }

    private static List<Long> rebuildPath(Map<Long, Long> parent, long dst) {
        LinkedList<Long> path = new LinkedList<>();
        for (Long cur = dst; cur != null; cur = parent.get(cur)) {
            path.addFirst(cur);
        }
        return path;
    }
}
