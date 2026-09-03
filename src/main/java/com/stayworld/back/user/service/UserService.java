package com.stayworld.back.user.service;

import com.stayworld.back.friend.repository.FriendRepository;
import com.stayworld.back.global.exception.UnauthorizedException;
import com.stayworld.back.reservation.repository.ReservationRepository;
import com.stayworld.back.user.dto.CreateDto;
import com.stayworld.back.user.dto.DeleteDto;
import com.stayworld.back.user.dto.ModifyDto;
import com.stayworld.back.user.dto.ProfilePictureDto;
import com.stayworld.back.user.dto.PublicStatsDto;
import com.stayworld.back.user.dto.PublicUserDto;
import com.stayworld.back.user.dto.UserDto;
import com.stayworld.back.user.dto.UserSearchDto;
import com.stayworld.back.user.entity.User;
import com.stayworld.back.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int PROFILE_PICTURE_COUNT = 10;

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final FriendRepository friendRepository;

    @Transactional
    public UserDto createUser(CreateDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(encodedPassword);
        user.setNickname(dto.getNickname());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setBalance(50000);
        user.setVisitorCount(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setProfilePictureId(ThreadLocalRandom.current().nextInt(PROFILE_PICTURE_COUNT));

        return toDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(long id) {
        return toDto(findMemberById(id));
    }

    /** 타인에게 보여줄 공개 프로필 (민감 정보 제외). */
    @Transactional(readOnly = true)
    public PublicUserDto getPublicProfile(long id) {
        User user = findMemberById(id);
        PublicUserDto dto = new PublicUserDto();
        dto.setUserId(user.getId());
        dto.setNickname(user.getNickname());
        dto.setVisitorCount(user.getVisitorCount());
        dto.setMemberSince(user.getCreatedAt());
        return dto;
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
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("비밀번호가 일치하지 않습니다.");
        }
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public List<UserSearchDto> searchByNickname(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        return userRepository
            .findByNicknameContaining(keyword)
            .stream()
            .map(user -> {
                UserSearchDto dto = new UserSearchDto();
                dto.setId(user.getId());
                dto.setNickname(user.getNickname());
                return dto;
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public PublicStatsDto getPublicStats(long userId) {
        findMemberById(userId);

        PublicStatsDto dto = new PublicStatsDto();
        dto.setVisitedGuesthouseCount(
            reservationRepository.countDistinctVisitedGuesthouses(
                userId,
                LocalDate.now()
            )
        );
        dto.setFriendCount(friendRepository.countByUserId(userId));
        return dto;
    }

    private User findMemberById(long id) {
        return userRepository
            .findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("존재하지 않는 회원입니다.")
            );
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
        dto.setProfilePictureId(user.getProfilePictureId());
        return dto;
    }

    public ProfilePictureDto getProfilePictureId(long userId) {
        User user = findMemberById(userId);
        return new ProfilePictureDto(user.getProfilePictureId());
    }

    public void modifyProfilePictureId(long userId, int profilePictureId) {
        User user = findMemberById(userId);
        user.setProfilePictureId(profilePictureId);
        userRepository.save(user);
    }
}
