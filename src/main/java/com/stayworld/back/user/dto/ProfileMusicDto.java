package com.stayworld.back.user.dto;

import com.stayworld.back.user.entity.ProfileMusic;

public record ProfileMusicDto(Long id, Long musicId, String title, String artist) {

    public static ProfileMusicDto from(ProfileMusic profileMusic) {
        return new ProfileMusicDto(
                profileMusic.getId(),
                profileMusic.getMusic().getId(),
                profileMusic.getMusic().getTitle(),
                profileMusic.getMusic().getArtist()
        );
    }
}
