package com.stayworld.back.global.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 현재 로그인한 회원 id(Long)를 컨트롤러 파라미터로 주입한다.
 * 로그인 세션이 없으면 {@link com.stayworld.back.global.exception.UnauthorizedException} → 401.
 *
 * <p>세션에는 user 도메인({@code UserController#login})이
 * {@link LoginMemberArgumentResolver#SESSION_KEY} 키로 회원 id를 넣는다.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginMember {
}
