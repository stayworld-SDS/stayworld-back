package com.stayworld.back.wave.dto;

/**
 * 추천 카드에 붙는 "왜 떴는지" 한 줄.
 *
 * <p>{@code type}: {@code CO_VISITOR} | {@code MUTUAL_FRIEND} | {@code CO_REGION} | {@code RANDOM}
 */
public record Reason(String type, String label) {
}
