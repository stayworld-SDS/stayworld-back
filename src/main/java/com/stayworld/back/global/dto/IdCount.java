package com.stayworld.back.global.dto;

/**
 * {@code SELECT x AS id, COUNT(...) AS count ... GROUP BY x} 형태 집계 결과의 공용 프로젝션.
 * 쿼리에서 별칭을 반드시 {@code id}, {@code count} 로 맞춰야 한다.
 */
public interface IdCount {
    Long getId();

    long getCount();
}
