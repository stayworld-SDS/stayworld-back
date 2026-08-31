package com.stayworld.back.user.service;

import com.stayworld.back.global.exception.UnauthorizedException;
import com.stayworld.back.user.dto.CreateDto;
import com.stayworld.back.user.dto.DeleteDto;
import com.stayworld.back.user.dto.ModifyDto;
import com.stayworld.back.user.dto.UserDto;
import com.stayworld.back.user.entity.User;
import com.stayworld.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserDto createUser(CreateDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setNickname(dto.getNickname());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setBalance(50000);
        user.setVisitorCount(0);
        user.setCreatedAt(LocalDateTime.now());

        return toDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(long id) {
        return toDto(findMemberById(id));
    }

    @Transactional(readOnly = true)
    public boolean checkEmailOccupancy(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public UserDto modifyUserDetails(long id, ModifyDto dto) {
        User user = findMemberById(id);
        user.setNickname(dto.getNickname());
        user.setPhoneNumber(dto.getPhoneNumber());
        return toDto(user);
    }

    @Transactional
    public void deleteUser(long id, DeleteDto dto) {
        User user = findMemberById(id);
        if (!user.getPassword().equals(dto.getPassword())) {
            throw new UnauthorizedException("비밀번호가 일치하지 않습니다.");
        }
        userRepository.delete(user);
    }

    private User findMemberById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setNickname(user.getNickname());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setBalance(user.getBalance());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setVisitorCount(user.getVisitorCount());
        return dto;
    }
}
