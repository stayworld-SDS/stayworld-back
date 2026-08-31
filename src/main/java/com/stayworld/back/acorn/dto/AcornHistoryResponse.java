package com.stayworld.back.acorn.dto;

import com.stayworld.back.acorn.entity.AcornHistory;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

/** GET /acorns/history 응답. */
public record AcornHistoryResponse(
        List<Item> history,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
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

    public static AcornHistoryResponse from(Page<AcornHistory> page) {
        return new AcornHistoryResponse(
                page.getContent().stream().map(Item::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
