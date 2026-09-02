package com.stayworld.back.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProfileMusicAddRequest {

    @NotNull
    Long musicId;
}
