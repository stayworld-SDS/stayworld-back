package com.stayworld.back.user.service;

import com.stayworld.back.global.exception.UnauthorizedException;
import com.stayworld.back.user.dto.DeleteDto;
import com.stayworld.back.user.dto.LoginDto;
import com.stayworld.back.user.dto.ModifyDto;
import com.stayworld.back.user.dto.UserDto;
import com.stayworld.back.user.entity.Member;
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
    public UserDto createUser(UserDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Member member = new Member();
        member.setEmail(dto.getEmail());
        member.setPassword(dto.getPassword());
        member.setNickname(dto.getNickname());
        member.setPhoneNumber(dto.getPhoneNumber());
        member.setBalance(0);
        member.setVisitorCount(0);
        member.setCreated_at(LocalDateTime.now());

        return toDto(userRepository.save(member));
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
        Member member = findMemberById(id);
        member.setNickname(dto.getNickname());
        member.setPhoneNumber(dto.getPhoneNumber());
        return toDto(member);
    }

    @Transactional
    public void deleteUser(long id, DeleteDto dto) {
        Member member = findMemberById(id);
        if (!member.getPassword().equals(dto.getPassword())) {
            throw new UnauthorizedException("비밀번호가 일치하지 않습니다.");
        }
        userRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public long login(LoginDto dto) {
        Member member = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UnauthorizedException("이메일 또는 비밀번호가 일치하지 않습니다."));

        if (!member.getPassword().equals(dto.getPassword())) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        return member.getId();
    }

    private Member findMemberById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    private UserDto toDto(Member member) {
        UserDto dto = new UserDto();
        dto.setId(member.getId());
        dto.setEmail(member.getEmail());
        dto.setNickname(member.getNickname());
        dto.setPhoneNumber(member.getPhoneNumber());
        dto.setBalance(member.getBalance());
        dto.setCreatedAt(member.getCreated_at());
        dto.setVisitorCount(member.getVisitorCount());
        return dto;
    }
}
