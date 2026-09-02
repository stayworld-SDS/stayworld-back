package com.stayworld.back.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 타인이 볼 수 있는 미니홈피 프로필. {@code UserDto} 와 달리 email/전화번호/도토리 잔액 등
 * 민감 정보는 빼고, 본인 정보는 {@code GET /users/me} 로만 노출한다.
 */
@Data
public class PublicUserDto {
    long userId;
    String nickname;
    int visitorCount;
    LocalDateTime memberSince;
}
