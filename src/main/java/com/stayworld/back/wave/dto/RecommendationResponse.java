package com.stayworld.back.wave.dto;

import java.util.List;

public record RecommendationResponse(List<RecommendedUser> recommendations) {
}
