package com.stayworld.back.acorn.exception;

/**
 * 도토리 차감 시 잔액이 부족할 때. {@link IllegalArgumentException} 을 상속해
 * GlobalExceptionHandler 의 기존 400 매핑을 그대로 탄다.
 */
public class InsufficientAcornException extends IllegalArgumentException {

    public InsufficientAcornException(int balance, int required) {
        super("도토리가 부족합니다. (보유 " + balance + ", 필요 " + required + ")");
    }
}
