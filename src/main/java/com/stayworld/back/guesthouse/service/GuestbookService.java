package com.stayworld.back.guesthouse.service;

import com.stayworld.back.global.exception.NotFoundException;
import com.stayworld.back.guesthouse.dto.GuestbookCreateRequest;
import com.stayworld.back.guesthouse.dto.GuestbookSummaryDto;
import com.stayworld.back.guesthouse.dto.GuestbookPageResponse;
import com.stayworld.back.guesthouse.entity.Guestbook;
import com.stayworld.back.guesthouse.entity.Guesthouse;
import com.stayworld.back.guesthouse.exception.GuesthouseNotFoundException;
import com.stayworld.back.guesthouse.exception.GuestbookEligibilityException;
import com.stayworld.back.guesthouse.repository.GuestbookRepository;
import com.stayworld.back.guesthouse.repository.GuesthouseRepository;
import com.stayworld.back.user.entity.User;
import com.stayworld.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class GuestbookService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int GUESTBOOK_PAGE_SIZE = 5;

    private final GuestbookRepository guestbookRepository;
    private final GuesthouseRepository guesthouseRepository;
    private final UserRepository userRepository;

    @Transactional
    public void saveGuestbook(Long userId, long guesthouseId, GuestbookCreateRequest guestbookCreateRequest){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));
        Guesthouse guesthouse = guesthouseRepository.findById(guesthouseId)
                .orElseThrow(GuesthouseNotFoundException::new);

        Guestbook guestbook = new Guestbook();
        guestbook.setGuesthouse(guesthouse);
        guestbook.setUser(user);
        guestbook.setBody(guestbookCreateRequest.getBody());
        try {
            guestbookRepository.saveAndFlush(guestbook);
        } catch (DataIntegrityViolationException e) {
            throw new GuestbookEligibilityException();
        }
    }

    @Transactional(readOnly = true)
    public GuestbookPageResponse findByGuesthouseId(long guesthouseId, int page){

        if (page < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다.");
        }

        if (!guesthouseRepository.existsById(guesthouseId)){
            throw new GuesthouseNotFoundException();
        }

        PageRequest pageable = PageRequest.of(
                page,
                GUESTBOOK_PAGE_SIZE,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );
        Page<GuestbookSummaryDto> guestbooks = guestbookRepository
                .findByGuesthouseId(guesthouseId, pageable)
                .map(entity -> {
                    GuestbookSummaryDto dto = new GuestbookSummaryDto();
                    dto.setWriter(entity.getUser().getNickname());
                    dto.setBody(entity.getBody());
                    dto.setCreatedAt(entity.getCreatedAt());
                    return dto;
                });

        return GuestbookPageResponse.from(guestbooks);
    }
}
