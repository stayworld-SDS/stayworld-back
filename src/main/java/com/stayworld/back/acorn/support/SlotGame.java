package com.stayworld.back.acorn.support;

/**
 * 777 슬롯 배당 로직 (순수 함수).
 *
 * <pre>
 *   7 7 7        → +7777   GAME_JACKPOT
 *   그 외 트리플  → +500    GAME_TRIPLE
 *   페어(2개 일치) → +100    GAME_PAIR
 *   전부 다름     → -50     GAME_LOSE
 * </pre>
 */
public final class SlotGame {

    public static final String JACKPOT = "GAME_JACKPOT";
    public static final String TRIPLE = "GAME_TRIPLE";
    public static final String PAIR = "GAME_PAIR";
    public static final String LOSE = "GAME_LOSE";

    private static final int JACKPOT_NUMBER = 7;

    private SlotGame() {
    }

    public record Result(int delta, String reason) {
    }

    public static Result evaluate(int first, int second, int third) {
        boolean allSame = first == second && second == third;
        if (allSame) {
            return first == JACKPOT_NUMBER
                    ? new Result(7777, JACKPOT)
                    : new Result(500, TRIPLE);
        }
        boolean anyPair = first == second || second == third || first == third;
        return anyPair
                ? new Result(100, PAIR)
                : new Result(-50, LOSE);
    }
}
