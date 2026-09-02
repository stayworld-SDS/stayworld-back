package com.stayworld.back.friend.dto;

/** 촌수 경로의 노드 하나 (나 → ... → 대상). */
public record PathNode(Long userId, String nickname) {
}
