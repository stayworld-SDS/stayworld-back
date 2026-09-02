package com.stayworld.back.guesthouse.support;

/**
 * 게스트하우스 주소에서 광역 지역명을 뽑는다 (추천의 "같은 지역" 매칭용).
 *
 * <p>주소 첫 토큰에서 행정구역 접미사를 떼는 수준의 근사치다.
 * "제주시 애월읍 1길 10" → {@code "제주"}, "강원도 평창군 …" → {@code "강원"},
 * "서울특별시 마포구 …" → {@code "서울"}. 매칭 실패 시 첫 토큰을 그대로 돌려준다.
 */
public final class RegionExtractor {

    private static final String[] SUFFIXES = {
            "특별자치도", "특별자치시", "특별시", "광역시", "자치시", "자치도"
    };

    private RegionExtractor() {
    }

    public static String from(String address) {
        if (address == null || address.isBlank()) {
            return "";
        }
        String head = address.strip().split("\\s+")[0];

        for (String suffix : SUFFIXES) {
            if (head.length() > suffix.length() && head.endsWith(suffix)) {
                return head.substring(0, head.length() - suffix.length());
            }
        }
        if (head.length() > 1 && (head.endsWith("도") || head.endsWith("시"))) {
            return head.substring(0, head.length() - 1);
        }
        return head;
    }
}
