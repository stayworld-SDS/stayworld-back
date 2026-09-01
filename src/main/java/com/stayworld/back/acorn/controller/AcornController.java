package com.stayworld.back.acorn.controller;

import com.stayworld.back.acorn.dto.AcornHistoryResponse;
import com.stayworld.back.acorn.dto.AcornMeResponse;
import com.stayworld.back.acorn.dto.GamePlayRequest;
import com.stayworld.back.acorn.dto.GamePlayResponse;
import com.stayworld.back.acorn.service.AcornService;
import com.stayworld.back.global.auth.LoginMember;
import com.stayworld.back.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AcornController {

    private final AcornService acornService;

    // 게임 참여 (슬롯 연출/판정은 프론트, 여기선 참여비 차감 + 획득량 반영만. 하루 10회 제한)
    @PostMapping("/games")
    public ApiResponse<GamePlayResponse> playGame(@LoginMember Long userId,
                                                   @Valid @RequestBody GamePlayRequest request) {
        return ApiResponse.success(acornService.play(userId, request.getWinAmount()));
    }

    // 도토리 사용/습득 내역 조회 (페이지네이션). ?page=0&size=20&sort=id,desc
    @GetMapping("/acorns/history")
    public ApiResponse<AcornHistoryResponse> getHistory(
            @LoginMember Long userId,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(acornService.history(userId, pageable));
    }

    // 내 현재 도토리 잔액, 오늘 게임 참여 여부
    @GetMapping("/acorns/me")
    public ApiResponse<AcornMeResponse> getMyAcorn(@LoginMember Long userId) {
        return ApiResponse.success(acornService.me(userId));
    }
}
