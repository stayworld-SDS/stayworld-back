package com.stayworld.back.reservation.support;

import java.util.Collection;
import java.util.Map;

/**
 * reservation 도메인이 숙소 정보를 읽어오는 포트(인터페이스).
 * guesthouse 도메인의 {@code GuesthouseReaderAdapter} 가 이 인터페이스를 구현해 빈으로 등록한다.
 */
public interface GuesthouseReader {

    /**
     * @throws com.stayworld.back.global.exception.NotFoundException 해당 숙소가 없을 때
     */
    GuesthouseInfo read(Long guesthouseId);

    /** 목록 조회용 배치 read. 존재하는 것만 담아 {@code id -> info} 로 반환. */
    Map<Long, GuesthouseInfo> readAll(Collection<Long> guesthouseIds);
}
