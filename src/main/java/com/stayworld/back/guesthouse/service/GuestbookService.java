package com.stayworld.back.guesthouse.service;

import com.stayworld.back.global.exception.NotFoundException;
import com.stayworld.back.guesthouse.dto.GuestbookDTO;
import com.stayworld.back.guesthouse.entity.Guestbook;
import com.stayworld.back.guesthouse.entity.Guesthouse;
import com.stayworld.back.guesthouse.repository.GuestbookRepository;
import com.stayworld.back.guesthouse.repository.GuesthouseRepository;
import com.stayworld.back.user.entity.User;
import com.stayworld.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GuestbookService {

    private final GuestbookRepository guestbookRepository;
    private final GuesthouseRepository guesthouseRepository;
    private final UserRepository userRepository;

    public void saveGuestbook(long guesthouseId, GuestbookDTO guestbookDto){
        // TODO: 실제로 숙박한 적이 있는지 확인하는 로직 추가 (reservation 도메인 연계)
        // TODO: custom exception(숙박내역없음)
        User user = userRepository.findById(guestbookDto.getUserId())
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));
        Guesthouse guesthouse = guesthouseRepository.findById(guesthouseId)
                .orElseThrow(() -> new NotFoundException("게스트하우스를 찾을 수 없습니다."));
        Guestbook guestbook = new Guestbook();
        guestbook.setGuesthouse(guesthouse);
        guestbook.setUser(user);
        guestbook.setCreatedAt(LocalDateTime.now());
        guestbook.setBody(guestbookDto.getBody());
        guestbookRepository.save(guestbook);

    }
}
