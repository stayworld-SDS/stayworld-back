package com.stayworld.back.friend.dto;

import java.util.List;

/**
 * 로그인 유저 ↔ 대상 유저의 촌수.
 *
 * <p>{@code degree} 는 경로 간선 수(직접 일촌 = 1). {@code KinshipCalculator.MAX_DEPTH}
 * 안에 닿지 못하면 {@code reachable=false, degree=null, path=[]}.
 */
public record DegreeResponse(
        Long targetUserId,
        boolean reachable,
        Integer degree,
        List<PathNode> path
) {
    public static DegreeResponse reachable(Long targetUserId, List<PathNode> path) {
        return new DegreeResponse(targetUserId, true, path.size() - 1, path);
    }

    public static DegreeResponse unreachable(Long targetUserId) {
        return new DegreeResponse(targetUserId, false, null, List.of());
    }
}
