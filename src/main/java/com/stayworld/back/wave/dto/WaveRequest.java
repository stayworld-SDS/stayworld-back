package com.stayworld.back.wave.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WaveRequest {
    @NotNull
    private Long targetUserId;
}
