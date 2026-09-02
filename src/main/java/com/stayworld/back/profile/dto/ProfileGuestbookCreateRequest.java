package com.stayworld.back.profile.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileGuestbookCreateRequest {
    @NotBlank(message = "방명록 본문은 필수입니다.")
    private String body;
}
