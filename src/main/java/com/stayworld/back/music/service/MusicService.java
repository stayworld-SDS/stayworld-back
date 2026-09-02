package com.stayworld.back.music.service;

import com.stayworld.back.music.dto.MusicCreateRequest;
import com.stayworld.back.music.dto.MusicDto;
import com.stayworld.back.music.entity.Music;
import com.stayworld.back.music.repository.MusicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MusicService {

    private final MusicRepository musicRepository;

    @Transactional
    public MusicDto createMusic(MusicCreateRequest request) {
        Music music = Music.builder()
                .title(request.getTitle())
                .artist(request.getArtist())
                .build();

        return MusicDto.from(musicRepository.save(music));
    }
}
