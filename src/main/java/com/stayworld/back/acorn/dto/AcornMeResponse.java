package com.stayworld.back.acorn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** GET /acorns/me 응답. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcornMeResponse {

    private int balance;
    private int playCount;    // 오늘 게임 참여 횟수
    private int dailyLimit;   // 하루 참여 제한 (현재 10)
}
