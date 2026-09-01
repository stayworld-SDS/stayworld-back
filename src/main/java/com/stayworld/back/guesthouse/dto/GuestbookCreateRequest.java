package com.stayworld.back.guesthouse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuestbookCreateRequest {
    @NotBlank(message = "방명록 본문은 필수입니다.")
    @Size(min = 10, max = 500, message = "방명록 본문은 10자 이상 500자 이하여야 합니다.")
    public String body;
}
