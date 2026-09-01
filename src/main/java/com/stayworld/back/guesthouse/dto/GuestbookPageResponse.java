package com.stayworld.back.guesthouse.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record GuestbookPageResponse(
        List<GuestbookSummaryDto> guestbooks,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static GuestbookPageResponse from(Page<GuestbookSummaryDto> page) {
        return new GuestbookPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
