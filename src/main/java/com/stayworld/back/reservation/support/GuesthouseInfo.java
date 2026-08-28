package com.stayworld.back.reservation.support;

/**
 * reservation 도메인이 응답을 구성하기 위해 필요한 숙소 정보 조각.
 * guesthouse 도메인 엔티티에 직접 의존하지 않으려고 reservation 쪽에서 정의한 뷰 모델.
 */
public record GuesthouseInfo(
        Long id,
        String name,
        int price,          // 1박 요금
        String address,
        int capacity,       // 최대 수용 인원
        boolean parking,
        boolean wifi,
        boolean breakfast
) {
}
