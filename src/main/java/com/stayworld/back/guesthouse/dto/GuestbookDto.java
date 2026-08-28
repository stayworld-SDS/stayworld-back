package com.stayworld.back.guesthouse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuestbookDto {
    public long userId;
    public String body;
}
