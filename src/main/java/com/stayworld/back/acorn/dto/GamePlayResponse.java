package com.stayworld.back.acorn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** POST /games 응답. acorns = 참여비 차감 + 획득분 반영 후 남은 도토리 잔액. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GamePlayResponse {

    private int acorns;
}
