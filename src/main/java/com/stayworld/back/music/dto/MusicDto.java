package com.stayworld.back.music.dto;

import com.stayworld.back.music.entity.Music;

public record MusicDto(Long id, String title, String artist) {

    public static MusicDto from(Music music) {
        return new MusicDto(music.getId(), music.getTitle(), music.getArtist());
    }
}
