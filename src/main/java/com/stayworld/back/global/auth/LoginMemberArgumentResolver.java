package com.stayworld.back.global.auth;

import com.stayworld.back.global.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@code @LoginMember Long memberId} 파라미터를 세션에서 채운다.
 * 세션이 없거나 회원 id가 없으면 {@link UnauthorizedException}(401).
 */
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    /** user 도메인이 로그인 시 회원 id를 넣는 세션 속성 키. 이 값의 단일 출처. */
    public static final String SESSION_KEY = "MEMBER_ID";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpSession session = request != null ? request.getSession(false) : null;
        Object value = session != null ? session.getAttribute(SESSION_KEY) : null;

        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new UnauthorizedException();
    }
}
