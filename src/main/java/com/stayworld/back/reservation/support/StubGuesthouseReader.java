package com.stayworld.back.reservation.support;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ⚠️ 임시 스텁 - guesthouse 도메인이 {@link GuesthouseReader} 구현체를 빈으로 제공하기 전까지만 사용.
 * 실제 숙소를 조회하지 않고 고정 더미를 돌려준다. (숙소 없음 → 404 경로는 실제 어댑터에서 구현)
 * guesthouse 도메인 연동 시 이 파일 삭제.
 */
@Component
public class StubGuesthouseReader implements GuesthouseReader {

    @Override
    public GuesthouseInfo read(Long guesthouseId) {
        return new GuesthouseInfo(
                guesthouseId,
                "임시 게스트하우스 #" + guesthouseId,
                50_000,
                "서울특별시 어딘가로 123",
                4,
                true,
                true,
                false
        );
    }

    @Override
    public Map<Long, GuesthouseInfo> readAll(Collection<Long> guesthouseIds) {
        return guesthouseIds.stream()
                .distinct()
                .collect(Collectors.toMap(Function.identity(), this::read));
    }
}
