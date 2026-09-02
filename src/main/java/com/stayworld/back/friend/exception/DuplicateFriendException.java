package com.stayworld.back.friend.exception;

import com.stayworld.back.global.exception.ConflictException;

public class DuplicateFriendException extends ConflictException {
    public DuplicateFriendException() {
        super("이미 일촌인 유저입니다.");
    }
}
