package com.stayworld.back.guesthouse.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GuestbookSummaryDto {
    String writer;
    String body;
    LocalDateTime createdAt;
}
