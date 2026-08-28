package com.stayworld.back.acorn.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlotGameTest {

    @Test
    void 세븐_트리플은_잭팟() {
        SlotGame.Result r = SlotGame.evaluate(7, 7, 7);

        assertThat(r.delta()).isEqualTo(7777);
        assertThat(r.reason()).isEqualTo(SlotGame.JACKPOT);
    }

    @Test
    void 세븐이_아닌_트리플은_트리플_보상() {
        SlotGame.Result r = SlotGame.evaluate(3, 3, 3);

        assertThat(r.delta()).isEqualTo(500);
        assertThat(r.reason()).isEqualTo(SlotGame.TRIPLE);
    }

    @Test
    void 영_트리플도_잭팟은_아님() {
        assertThat(SlotGame.evaluate(0, 0, 0).reason()).isEqualTo(SlotGame.TRIPLE);
    }

    @Test
    void 두_개만_일치하면_페어_보상() {
        assertThat(SlotGame.evaluate(1, 1, 9).delta()).isEqualTo(100);
        assertThat(SlotGame.evaluate(1, 9, 1).delta()).isEqualTo(100);
        assertThat(SlotGame.evaluate(9, 1, 1).reason()).isEqualTo(SlotGame.PAIR);
    }

    @Test
    void 전부_다르면_손실() {
        SlotGame.Result r = SlotGame.evaluate(1, 2, 3);

        assertThat(r.delta()).isEqualTo(-50);
        assertThat(r.reason()).isEqualTo(SlotGame.LOSE);
    }
}
