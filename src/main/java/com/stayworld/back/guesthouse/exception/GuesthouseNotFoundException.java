package com.stayworld.back.guesthouse.exception;

import com.stayworld.back.global.exception.NotFoundException;

public class GuesthouseNotFoundException extends NotFoundException {
    public GuesthouseNotFoundException() {
        super("게스트하우스를 찾을 수 없습니다.");
    }
}
