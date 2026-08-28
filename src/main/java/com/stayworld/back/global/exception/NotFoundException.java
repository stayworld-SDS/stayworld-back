package com.stayworld.back.global.exception;

/**
 * 요청한 리소스를 찾을 수 없을 때. GlobalExceptionHandler 에서 404 로 매핑한다.
 * 모든 도메인이 공용으로 쓴다.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
