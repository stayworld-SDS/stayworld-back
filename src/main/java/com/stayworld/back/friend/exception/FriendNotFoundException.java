package com.stayworld.back.friend.exception;

import com.stayworld.back.global.exception.NotFoundException;

public class FriendNotFoundException extends NotFoundException {
    public FriendNotFoundException() {
        super("일촌 관계를 찾을 수 없습니다.");
    }
}
