package com.stayworld.back.profile.service;

import com.stayworld.back.global.exception.NotFoundException;
import com.stayworld.back.guesthouse.dto.GuestbookPageResponse;
import com.stayworld.back.guesthouse.dto.GuestbookSummaryDto;
import com.stayworld.back.profile.dto.ProfileGuestbookCreateRequest;
import com.stayworld.back.profile.entity.ProfileGuestbook;
import com.stayworld.back.profile.repository.ProfileGuestbookRepository;
import com.stayworld.back.user.entity.User;
import com.stayworld.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileGuestbookService {

    private static final int PAGE_SIZE = 5;

    private final ProfileGuestbookRepository profileGuestbookRepository;
    private final UserRepository userRepository;

    public GuestbookPageResponse findByOwnerId(long ownerId, int page) {
        if (page < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다.");
        }
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException("유저를 찾을 수 없습니다.");
        }

        PageRequest pageable = PageRequest.of(
                page,
                PAGE_SIZE,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );
        Page<ProfileGuestbook> entries = profileGuestbookRepository.findByOwnerId(ownerId, pageable);

        Map<Long, String> nicknameById = userRepository.findAllById(
                        entries.getContent().stream().map(ProfileGuestbook::getWriterId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));

        return GuestbookPageResponse.from(entries.map(entry -> {
            GuestbookSummaryDto dto = new GuestbookSummaryDto();
            dto.setWriterId(entry.getWriterId());
            dto.setWriter(nicknameById.get(entry.getWriterId()));
            dto.setBody(entry.getBody());
            dto.setCreatedAt(entry.getCreatedAt());
            return dto;
        }));
    }

    @Transactional
    public void write(long ownerId, Long writerId, ProfileGuestbookCreateRequest request) {
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException("유저를 찾을 수 없습니다.");
        }
        if (!userRepository.existsById(writerId)) {
            throw new NotFoundException("작성자를 찾을 수 없습니다.");
        }

        ProfileGuestbook entry = ProfileGuestbook.builder()
                .ownerId(ownerId)
                .writerId(writerId)
                .body(request.getBody())
                .build();
        profileGuestbookRepository.save(entry);
    }
}
