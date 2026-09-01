package com.stayworld.back.acorn.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POST /games 요청 본문. 슬롯 연출/판정은 프론트에서 하고, 그 결과로 획득한 도토리만 넘긴다.
 * 참여비({@code GAME_ENTRY})는 서버가 고정액으로 별도 차감하므로 여기엔 없다.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GamePlayRequest {

    @Min(value = 0, message = "획득량은 음수일 수 없습니다.")
    private int winAmount;
}
