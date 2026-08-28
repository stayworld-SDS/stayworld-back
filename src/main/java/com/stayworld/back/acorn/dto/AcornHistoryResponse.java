package com.stayworld.back.acorn.dto;

import com.stayworld.back.acorn.entity.AcornHistory;

import java.time.LocalDateTime;
import java.util.List;

/** GET /acorns/history 응답. */
public record AcornHistoryResponse(List<Item> history) {

    public record Item(
            String reason,
            int amount,
            int balance,        // 거래 직후 잔액
            LocalDateTime createdAt
    ) {
        public static Item from(AcornHistory h) {
            return new Item(h.getReason(), h.getAmount(), h.getBalanceAfter(), h.getCreatedAt());
        }
    }

    public static AcornHistoryResponse from(List<AcornHistory> histories) {
        return new AcornHistoryResponse(histories.stream().map(Item::from).toList());
    }
}
