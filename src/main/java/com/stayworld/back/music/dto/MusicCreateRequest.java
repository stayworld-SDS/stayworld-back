package com.stayworld.back.music.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MusicCreateRequest {

    @NotBlank
    String title;

    @NotBlank
    String artist;
}
