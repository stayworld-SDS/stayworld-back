package com.stayworld.back.reservation.support;

import java.util.Collection;
import java.util.Map;

/**
 * reservation 도메인이 숙소 정보를 읽어오는 포트(인터페이스).
 * guesthouse 도메인(또는 그 어댑터)이 이 인터페이스를 구현해 빈으로 등록하면 된다.
 * 그 전까지는 {@link StubGuesthouseReader} 가 임시로 채운다.
 */
public interface GuesthouseReader {

    /**
     * @throws com.stayworld.back.global.exception.NotFoundException 해당 숙소가 없을 때
     */
    GuesthouseInfo read(Long guesthouseId);

    /** 목록 조회용 배치 read. 존재하는 것만 담아 {@code id -> info} 로 반환. */
    Map<Long, GuesthouseInfo> readAll(Collection<Long> guesthouseIds);
}
