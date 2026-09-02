package com.stayworld.back.wave.controller;

import com.stayworld.back.global.auth.LoginMember;
import com.stayworld.back.global.response.ApiResponse;
import com.stayworld.back.wave.dto.RecommendationResponse;
import com.stayworld.back.wave.dto.WaveMeResponse;
import com.stayworld.back.wave.dto.WaveRequest;
import com.stayworld.back.wave.dto.WaveResponse;
import com.stayworld.back.wave.service.RecommendationService;
import com.stayworld.back.wave.service.WaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/waves")
@RequiredArgsConstructor
public class WaveController {

    private final RecommendationService recommendationService;
    private final WaveService waveService;

    // 사람 추천 피드 (부수효과 없음). withDegree=true 면 각 카드에 나로부터의 촌수도 채운다.
    @GetMapping("/recommendations")
    public ApiResponse<RecommendationResponse> getRecommendations(
            @LoginMember Long userId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "false") boolean withDegree) {
        return ApiResponse.success(recommendationService.recommend(userId, limit, withDegree));
    }

    // 추천에서 한 명 골라 놀러가기: 대상 방문자 수 +1, 그날 첫 파도타기면 도토리 +1.
    @PostMapping
    public ApiResponse<WaveResponse> wave(
            @LoginMember Long userId,
            @Valid @RequestBody WaveRequest request) {
        return ApiResponse.success("파도를 타고 놀러갔어요.", waveService.visit(userId, request.getTargetUserId()));
    }

    // 오늘 내 파도타기 현황.
    @GetMapping("/me")
    public ApiResponse<WaveMeResponse> getMyWaveStatus(@LoginMember Long userId) {
        return ApiResponse.success(waveService.today(userId));
    }
}
