package com.stayworld.back.music.controller;

import com.stayworld.back.global.response.ApiResponse;
import com.stayworld.back.music.dto.MusicCreateRequest;
import com.stayworld.back.music.dto.MusicDto;
import com.stayworld.back.music.service.MusicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/musics")
@RequiredArgsConstructor
public class MusicController {

    private final MusicService musicService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MusicDto> createMusic(@Valid @RequestBody MusicCreateRequest request) {
        return ApiResponse.success("음악이 등록되었습니다.", musicService.createMusic(request));
    }
}
