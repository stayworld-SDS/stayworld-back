package com.stayworld.back.acorn.support;

import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 슬롯 릴 난수 생성. 테스트에서 갈아끼울 수 있도록 빈으로 분리.
 */
@Component
public class GameRandom {

    private final Random random = new Random();

    /** 0~9 릴 3개. */
    public int[] roll() {
        return new int[] {
                random.nextInt(10),
                random.nextInt(10),
                random.nextInt(10)
        };
    }
}
