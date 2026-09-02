package com.stayworld.back.guesthouse.exception;

public class GuestbookEligibilityException extends IllegalArgumentException {

    public GuestbookEligibilityException() {
        super("방명록을 작성할 수 있는 숙박 내역이 없습니다.");
    }
}
