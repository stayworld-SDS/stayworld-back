package com.stayworld.back.acorn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POST /acorns/charge 요청 본문. 프론트가 넘긴 부호 있는 증감량을 그대로 반영한다.
 * 양수면 충전, 음수면 차감. 0 은 허용하지 않는다.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcornChargeRequest {

    private int amount;
}
