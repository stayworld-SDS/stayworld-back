package com.stayworld.back.user.service;

import com.stayworld.back.global.exception.NotFoundException;
import com.stayworld.back.music.entity.Music;
import com.stayworld.back.music.repository.MusicRepository;
import com.stayworld.back.user.dto.ProfileMusicAddRequest;
import com.stayworld.back.user.dto.ProfileMusicDto;
import com.stayworld.back.user.entity.ProfileMusic;
import com.stayworld.back.user.repository.ProfileMusicRepository;
import com.stayworld.back.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileMusicService {

    private final ProfileMusicRepository profileMusicRepository;
    private final MusicRepository musicRepository;
    private final UserRepository userRepository;

    public List<ProfileMusicDto> getPlaylist(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("존재하지 않는 회원입니다.");
        }

        return profileMusicRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ProfileMusicDto::from)
                .toList();
    }

    @Transactional
    public ProfileMusicDto addToPlaylist(Long userId, ProfileMusicAddRequest request) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("존재하지 않는 회원입니다.");
        }

        Music music = musicRepository.findById(request.getMusicId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 음악입니다."));

        ProfileMusic profileMusic = ProfileMusic.builder()
                .userId(userId)
                .music(music)
                .build();

        return ProfileMusicDto.from(profileMusicRepository.save(profileMusic));
    }
}
