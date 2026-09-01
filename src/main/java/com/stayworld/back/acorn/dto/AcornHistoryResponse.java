package com.stayworld.back.acorn.dto;

import com.stayworld.back.acorn.entity.AcornHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

/** GET /acorns/history 응답. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcornHistoryResponse {

    private List<Item> history;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        private String reason;
        private int amount;
        private int balance;        // 거래 직후 잔액
        private LocalDateTime createdAt;

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
