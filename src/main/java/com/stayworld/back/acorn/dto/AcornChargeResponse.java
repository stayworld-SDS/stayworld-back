package com.stayworld.back.acorn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** POST /acorns/charge 응답. acorns = 증감 반영 후 남은 도토리 잔액. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcornChargeResponse {

    private int acorns;
}
