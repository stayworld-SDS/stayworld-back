package com.stayworld.back.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 모든 Swagger 문서 내용(그룹 태그, 엔드포인트 설명, 필드 설명)을 이 파일 한 곳에서 관리한다.
 * 컨트롤러/DTO 코드는 애노테이션 없이 깨끗하게 두고, 문서는 여기서 경로+메서드 / 스키마명 기준으로 주입한다.
 */
@Configuration
public class SwaggerConfig {

    private static final String SESSION_COOKIE = "SESSION";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("StayWorld API")
                        .version("0.0.1")
                        .description("""
                                StayWorld API 명세서

                                - 모든 응답은 `{ success, message, data }` 공통 포맷으로 제공된다.
                                - 로그인은 세션 쿠키(SESSION) 기반으로 안전하게 관리된다.
                                """))
                .components(new Components().addSecuritySchemes(SESSION_COOKIE,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name(SESSION_COOKIE)))
                .addSecurityItem(new SecurityRequirement().addList(SESSION_COOKIE))
                .tags(List.of(
                        tag("인증 / 회원", "회원가입, 로그인/로그아웃, 내 정보 조회·수정"),
                        tag("게스트하우스", "숙소 검색·조회, 숙소 방명록(다녀간 후기)"),
                        tag("예약", "숙소 예약 생성/조회/취소"),
                        tag("일촌", "일촌(친구) 추가/삭제, 촌수 계산"),
                        tag("미니홈피 방명록", "내 미니홈피에 친구가 남기는 방명록, 방문 기록/발자국"),
                        tag("배경음악", "미니홈피 BGM 곡 카탈로그 + 유저별 재생목록"),
                        tag("도토리", "도토리(포인트) 잔액, 미니게임, 충전, 사용/습득 내역"),
                        tag("파도타기", "사람 추천 피드 + 추천 대상 방문(놀러가기)"),
                        tag("기타", "헬스체크 등 부가 엔드포인트")
                ));
    }

    @Bean
    public GlobalOpenApiCustomizer apiDocsCustomizer() {
        return openApi -> {
            openApi.getPaths().forEach((path, pathItem) ->
                    pathItem.readOperationsMap().forEach((method, operation) ->
                            applyOperationMeta(path, method, operation)));
            enrichSchemas(openApi);
        };
    }

    private static Tag tag(String name, String description) {
        return new Tag().name(name).description(description);
    }

    // ---- 엔드포인트별 태그 / 설명 / 파라미터 설명 ----

    private record OperationMeta(
            String tag,
            String summary,
            String description,
            Map<String, String> params,
            Map<String, String> codes
    ) {
        private OperationMeta(String tag, String summary, String description) {
            this(tag, summary, description, Map.of(), Map.of());
        }

        private OperationMeta(String tag, String summary, String description, Map<String, String> params) {
            this(tag, summary, description, params, Map.of());
        }
    }

    private static final Map<String, Map<PathItem.HttpMethod, OperationMeta>> OPERATIONS = new LinkedHashMap<>();

    private static void reg(String path, PathItem.HttpMethod method, OperationMeta meta) {
        OPERATIONS.computeIfAbsent(path, p -> new LinkedHashMap<>()).put(method, meta);
    }

    private static void reg(String path, PathItem.HttpMethod method, String tag, String summary) {
        reg(path, method, new OperationMeta(tag, summary, null));
    }

    private static void reg(String path, PathItem.HttpMethod method, String tag, String summary, String description) {
        reg(path, method, new OperationMeta(tag, summary, description));
    }

    private static void reg(String path, PathItem.HttpMethod method, String tag, String summary,
                             String description, Map<String, String> params) {
        reg(path, method, new OperationMeta(tag, summary, description, params));
    }

    /** codes: HTTP 상태코드 문자열("400" 등) → 설명. */
    private static void reg(String path, PathItem.HttpMethod method, String tag, String summary,
                             String description, Map<String, String> params, Map<String, String> codes) {
        reg(path, method, new OperationMeta(tag, summary, description, params, codes));
    }

    /** codes("400", "설명", "404", "설명2", ...) 형태로 응답코드 설명을 간단히 만든다. */
    private static Map<String, String> codes(String... kv) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put(kv[i], kv[i + 1]);
        }
        return map;
    }

    static {
        String AUTH = "인증 / 회원";
        String GUESTHOUSE = "게스트하우스";
        String RESERVATION = "예약";
        String FRIEND = "일촌";
        String GUESTBOOK = "미니홈피 방명록";
        String MUSIC = "배경음악";
        String ACORN = "도토리";
        String WAVE = "파도타기";
        String ETC = "기타";

        String UNAUTHENTICATED = "세션에 사용자 ID가 없는 경우 (미인증)";

        // --- 인증 / 회원 ---
        reg("/auth/login", PathItem.HttpMethod.POST, AUTH, "로그인",
                "이메일/비밀번호로 로그인한다. 성공 시 세션 쿠키(SESSION)가 발급된다.",
                Map.of(),
                codes("401", "이메일 또는 비밀번호가 일치하지 않는 경우"));
        reg("/auth/logout", PathItem.HttpMethod.POST, AUTH, "로그아웃", "세션을 종료한다.");
        reg("/users", PathItem.HttpMethod.POST, AUTH, "회원가입",
                "phoneNumber는 010-1234-5678 형식 필수. 초기 도토리 잔액 50000, 프로필사진 id는 0~9 중 랜덤 배정.",
                Map.of(),
                codes("400", "이미 존재하는 이메일일 경우"));
        reg("/users/{userId}", PathItem.HttpMethod.GET, AUTH, "타인 공개 프로필 조회",
                "미니홈피(/users/:id)에서 보여줄 공개 프로필 정보를 반환한다. 개인정보는 보호된다.",
                Map.of(),
                codes("404", "해당 유저가 존재하지 않는 경우"));
        reg("/users/me", PathItem.HttpMethod.GET, AUTH, "내 정보 전체 조회",
                "이메일, 전화번호, 도토리 잔액 등 내 정보를 모두 조회한다.",
                Map.of(),
                codes("401", UNAUTHENTICATED));
        reg("/users/me", PathItem.HttpMethod.PATCH, AUTH, "내 정보 수정", "닉네임/전화번호만 변경 가능.",
                Map.of(),
                codes("401", UNAUTHENTICATED));
        reg("/users/me", PathItem.HttpMethod.DELETE, AUTH, "회원 탈퇴", "비밀번호 확인 후 탈퇴, 세션도 함께 종료된다.",
                Map.of(),
                codes("401", "세션에 사용자 ID가 없거나 비밀번호가 일치하지 않는 경우"));
        reg("/users/search", PathItem.HttpMethod.GET, AUTH, "닉네임으로 유저 검색", null,
                Map.of("keyword", "닉네임에 포함되는 검색어"));
        reg("/users/{userId}/public-stats", PathItem.HttpMethod.GET, AUTH, "공개 통계 조회",
                "다녀온 숙소 수, 일촌 수 등 미니홈피에 보여줄 활동 지표를 조회한다.",
                Map.of(),
                codes("404", "해당 유저가 존재하지 않는 경우"));
        reg("/users/{userId}/profile-picture", PathItem.HttpMethod.GET, AUTH, "프로필 사진 id 조회");
        reg("/users/{userId}/profile-picture", PathItem.HttpMethod.PATCH, AUTH, "프로필 사진 변경", null,
                Map.of(),
                codes("401", "세션에 사용자 ID가 없거나, 있으나 URI로 전달된 사용자 ID와 일치하지 않는 경우"));
        reg("/users/check-email/{email}", PathItem.HttpMethod.GET, AUTH, "이메일 중복 확인");

        // --- 게스트하우스 ---
        reg("/guesthouses", PathItem.HttpMethod.GET, GUESTHOUSE, "숙소 검색",
                "기간·인원 조건을 만족하는 숙소를 검색한다. location은 주소 또는 숙소이름 중 하나만 포함돼도 매칭된다(OR 조건).",
                Map.of(
                        "location", "주소 또는 숙소이름에 포함되는 검색어",
                        "start", "체크인 날짜",
                        "end", "체크아웃 날짜",
                        "headCount", "투숙 인원"
                ),
                codes("400", "유효하지 않은 지역으로 검색한 경우"));
        reg("/guesthouses/{id}", PathItem.HttpMethod.GET, GUESTHOUSE, "숙소 상세 조회", null,
                Map.of(),
                codes("404", "유효하지 않은 숙소 ID인 경우"));
        reg("/guesthouses/{id}/guestbooks", PathItem.HttpMethod.GET, GUESTHOUSE, "숙소 방명록 목록",
                "해당 숙소에 남겨진 방명록을 최신순으로 페이지네이션 조회한다.",
                Map.of("page", "0부터 시작하는 페이지 번호"),
                codes("404", "숙소가 존재하지 않는 경우"));
        reg("/guesthouses/{id}/guestbooks", PathItem.HttpMethod.POST, GUESTHOUSE, "숙소 방명록 작성",
                "로그인한 유저가 해당 숙소에 방명록을 남긴다.",
                Map.of(),
                codes(
                        "400", "본문이 10~500자를 벗어난 경우",
                        "404", "숙소가 존재하지 않는 경우",
                        "401", UNAUTHENTICATED
                ));

        // --- 예약 ---
        reg("/reservations/me", PathItem.HttpMethod.GET, RESERVATION, "내 예약 목록",
                "체크아웃 전(예정된) 예약만 조회한다.",
                Map.of(),
                codes("401", "미인증"));
        reg("/reservations/history", PathItem.HttpMethod.GET, RESERVATION, "내 예약 이력",
                "체크아웃이 지난(다녀온) 예약을 최근 순으로 조회한다.",
                Map.of(),
                codes("401", "미인증"));
        reg("/reservations/{reservationId}", PathItem.HttpMethod.GET, RESERVATION, "예약 상세 조회", null,
                Map.of(),
                codes(
                        "404", "예약이 없거나 본인 예약이 아닌 경우 (존재 여부를 숨김)",
                        "401", "미인증"
                ));
        reg("/reservations", PathItem.HttpMethod.POST, RESERVATION, "예약 생성",
                "결제 금액은 1박 요금 × 박수 × 인원수로 계산되어 도토리 잔액에서 차감된다.",
                Map.of(),
                codes(
                        "400", "체크아웃이 체크인보다 앞 / 수용 가능 인원 초과 / 도토리 부족 / 필수값 누락",
                        "404", "숙소가 존재하지 않는 경우",
                        "401", "미인증"
                ));
        reg("/reservations/{reservationId}", PathItem.HttpMethod.DELETE, RESERVATION, "예약 취소",
                "체크인 전 예약을 취소하면 결제한 도토리가 전액 환불된다.",
                Map.of(),
                codes(
                        "400", "이미 체크인 날짜가 지난 경우",
                        "404", "예약이 없거나 본인 예약이 아닌 경우",
                        "500", "점유 인원 복구 실패 등 정합성 오류",
                        "401", "미인증"
                ));

        // --- 일촌 ---
        reg("/friends", PathItem.HttpMethod.GET, FRIEND, "내 일촌 목록", "created_at 최신순으로 정렬된다.",
                Map.of(),
                codes("401", "미인증"));
        reg("/friends/degree", PathItem.HttpMethod.GET, FRIEND, "나와 대상 유저의 촌수 계산",
                "나와 targetUserId 사이의 최단 촌수와 경로를 계산한다.",
                Map.of("targetUserId", "촌수를 계산할 대상 유저 ID"),
                codes(
                        "400", "본인을 대상으로 요청한 경우",
                        "404", "대상 유저가 없는 경우",
                        "401", "미인증"
                ));
        reg("/friends", PathItem.HttpMethod.POST, FRIEND, "일촌 추가", null,
                Map.of(),
                codes(
                        "400", "본인을 대상으로 요청한 경우",
                        "404", "대상 유저가 없는 경우",
                        "409", "이미 일촌인 유저인 경우",
                        "401", "미인증"
                ));
        reg("/friends/{targetUserId}", PathItem.HttpMethod.DELETE, FRIEND, "일촌 삭제", null,
                Map.of(),
                codes(
                        "404", "일촌 관계가 없는 경우",
                        "401", "미인증"
                ));
        reg("/users/{userId}/friends", PathItem.HttpMethod.GET, FRIEND, "타인의 일촌 목록 조회");

        // --- 미니홈피 방명록 / 방문 ---
        reg("/users/{userId}/visits", PathItem.HttpMethod.POST, GUESTBOOK, "미니홈피 방문 기록",
                "미니홈피 방문을 기록하고, 반영된 누적 방문자 수를 반환한다.",
                Map.of(),
                codes("401", "미인증"));
        reg("/users/{userId}/footprints", PathItem.HttpMethod.GET, GUESTBOOK, "미니홈피 발자국 목록",
                "방문자별 가장 최근 방문 기록, 최대 20개.");
        reg("/users/{userId}/guestbooks", PathItem.HttpMethod.GET, GUESTBOOK, "미니홈피 방명록 목록",
                "숙소 방명록과 동일한 페이지 구조로 조회한다.",
                Map.of("page", "0부터 시작하는 페이지 번호"),
                codes("404", "미니홈피 주인이 존재하지 않는 경우"));
        reg("/users/{userId}/guestbooks", PathItem.HttpMethod.POST, GUESTBOOK, "미니홈피 방명록 작성",
                "path의 userId는 미니홈피 주인, writerId는 글쓴이(나)를 의미한다.",
                Map.of("writerId", "글쓴이(나)의 유저 id"),
                codes(
                        "400", "본문이 빈 문자열인 경우",
                        "404", "미니홈피 주인 또는 작성자가 존재하지 않는 경우"
                ));

        // --- 배경음악 ---
        reg("/musics", PathItem.HttpMethod.POST, MUSIC, "곡 카탈로그 등록",
                "제목/아티스트로 새 곡을 등록한다. 등록된 곡의 id는 재생목록 추가에 사용한다.");
        reg("/users/{userId}/musics", PathItem.HttpMethod.GET, MUSIC, "미니홈피 재생목록 조회",
                "추가한 순서대로 최신순으로 조회한다.",
                Map.of(),
                codes("404", "해당 유저가 존재하지 않는 경우"));
        reg("/users/{userId}/musics", PathItem.HttpMethod.DELETE, MUSIC, "재생목록에서 곡 삭제",
                "내 재생목록에서만 곡을 삭제할 수 있다.",
                Map.of("musicId", "삭제할 곡의 카탈로그 id"),
                codes(
                        "404", "해당 음악이 존재하지 않는 경우",
                        "401", "세션에 사용자 ID가 없거나 URI로 전달된 사용자 ID와 일치하지 않는 경우"
                ));
        reg("/users/me/musics", PathItem.HttpMethod.POST, MUSIC, "내 재생목록에 곡 추가",
                "POST /musics로 등록한 곡의 id(musicId)를 내 재생목록에 추가한다.",
                Map.of(),
                codes(
                        "404", "존재하지 않는 음악인 경우",
                        "401", UNAUTHENTICATED
                ));

        // --- 도토리 ---
        reg("/games", PathItem.HttpMethod.POST, ACORN, "미니게임 참여",
                "슬롯 게임에 참여해 도토리를 획득한다. 하루 10회까지 참여할 수 있다.",
                Map.of(),
                codes(
                        "400", "오늘 참여 10회 초과 / 도토리 부족(참여비 100) / 획득량이 음수인 경우",
                        "401", "미인증"
                ));
        reg("/acorns/charge", PathItem.HttpMethod.POST, ACORN, "도토리 충전", "도토리 잔액을 충전한다.",
                Map.of(),
                codes(
                        "400", "amount가 0이거나, 차감액이 잔액보다 큰 경우(도토리 부족)",
                        "401", "미인증"
                ));
        reg("/acorns/history", PathItem.HttpMethod.GET, ACORN, "도토리 사용/습득 내역",
                "페이지네이션 조회. 기본 page=0, size=20, id 내림차순. size는 최대 100까지(초과 시 100으로 조정).",
                Map.of(),
                codes("401", "미인증"));
        reg("/acorns/me", PathItem.HttpMethod.GET, ACORN, "내 도토리 현황", "현재 잔액과 오늘 게임 참여 여부를 반환한다.",
                Map.of(),
                codes("401", "미인증"));

        // --- 파도타기 ---
        reg("/waves/recommendations", PathItem.HttpMethod.GET, WAVE, "추천 피드 조회",
                "친구의 친구, 같은 숙소·지역 방문자 등을 조합해 함께 어울릴만한 유저를 추천한다. "
                        + "withDegree=true면 각 카드에 나와의 촌수도 함께 보여준다.",
                Map.of(
                        "limit", "가져올 카드 개수 (기본 10, 1~30)",
                        "withDegree", "각 카드에 나와의 촌수를 채울지 여부"
                ),
                codes("401", "미인증"));
        reg("/waves", PathItem.HttpMethod.POST, WAVE, "파도타서 놀러가기",
                "추천 카드에서 한 명을 골라 방문한다. 대상의 방문자 수가 오르고, 그날 첫 파도타기면 도토리를 지급받는다. "
                        + "하루 20회까지 놀러갈 수 있다.",
                Map.of(),
                codes(
                        "400", "본인을 대상으로 요청했거나 오늘 파도타기 20회를 초과한 경우",
                        "404", "대상 유저가 없는 경우",
                        "401", "미인증"
                ));
        reg("/waves/me", PathItem.HttpMethod.GET, WAVE, "오늘 내 파도타기 현황",
                "오늘 파도탄 횟수, 보상 수령 여부, 일일 한도를 반환한다.",
                Map.of(),
                codes("401", "미인증"));

        // --- 기타 ---
        reg("/health", PathItem.HttpMethod.GET, ETC, "헬스체크", "서버가 살아있는지 확인한다.");
    }

    private static void applyOperationMeta(String path, PathItem.HttpMethod method, Operation operation) {
        Map<PathItem.HttpMethod, OperationMeta> byMethod = OPERATIONS.get(path);
        if (byMethod == null) {
            return;
        }
        OperationMeta meta = byMethod.get(method);
        if (meta == null) {
            return;
        }

        operation.setTags(List.of(meta.tag()));
        if (meta.summary() != null) {
            operation.setSummary(meta.summary());
        }
        if (meta.description() != null) {
            operation.setDescription(meta.description());
        }
        if (operation.getParameters() != null) {
            for (Parameter parameter : operation.getParameters()) {
                String description = meta.params().get(parameter.getName());
                if (description != null) {
                    parameter.setDescription(description);
                }
            }
        }

        meta.codes().forEach((code, description) ->
                operation.getResponses().addApiResponse(code, new ApiResponse().description(description)));
    }

    // ---- 응답/요청 스키마 필드 설명 ----

    private static final Map<String, Map<String, String>> SCHEMA_FIELD_DESCRIPTIONS = Map.ofEntries(
            Map.entry("CreateDto", Map.of(
                    "password", "비밀번호",
                    "email", "이메일",
                    "nickname", "닉네임",
                    "phoneNumber", "휴대전화 (010-1234-5678 형식)"
            )),
            Map.entry("UserDto", Map.of(
                    "id", "유저 식별자",
                    "email", "이메일",
                    "nickname", "닉네임",
                    "phoneNumber", "휴대전화",
                    "balance", "도토리 잔액",
                    "createdAt", "가입 시각",
                    "visitorCount", "미니홈피 방문자 수",
                    "profilePictureId", "프로필 이모지 ID"
            )),
            Map.entry("PublicUserDto", Map.of(
                    "userId", "사용자 ID",
                    "nickname", "닉네임",
                    "visitorCount", "방문자 수",
                    "memberSince", "가입 시점"
            )),
            Map.entry("PublicStatsDto", Map.of(
                    "visitedGuesthouseCount", "다녀온 숙소 수",
                    "friendCount", "일촌 수"
            )),
            Map.entry("ModifyDto", Map.of(
                    "nickname", "변경할 닉네임",
                    "phoneNumber", "변경할 휴대전화"
            )),
            Map.entry("DeleteDto", Map.of("password", "본인 확인용 비밀번호")),
            Map.entry("UserSearchDto", Map.of("id", "유저 식별자", "nickname", "닉네임")),
            Map.entry("ProfilePictureDto", Map.of("profilePictureId", "프로필 이모지 ID (0~9)")),
            Map.entry("ProfileMusicDto", Map.of(
                    "id", "재생목록 항목 ID",
                    "musicId", "음악 카탈로그 ID",
                    "title", "제목",
                    "artist", "아티스트"
            )),
            Map.entry("ProfileMusicAddRequest", Map.of("musicId", "추가할 음악 카탈로그 ID")),
            Map.entry("LoginDto", Map.of("email", "이메일", "password", "비밀번호")),

            Map.entry("GuesthouseDto", Map.ofEntries(
                    Map.entry("id", "숙소 ID"),
                    Map.entry("name", "숙소 이름"),
                    Map.entry("price", "1박 요금"),
                    Map.entry("phoneNumber", "전화번호"),
                    Map.entry("address", "주소"),
                    Map.entry("capacity", "수용 가능 인원"),
                    Map.entry("parkingProvided", "주차 가능 여부"),
                    Map.entry("wifiProvided", "와이파이 제공 여부"),
                    Map.entry("breakfastProvided", "조식 제공 여부"),
                    Map.entry("introduction", "소개글"),
                    Map.entry("visitorCount", "누적 방문자 수"),
                    Map.entry("music", "배경음악")
            )),
            Map.entry("GuestbookCreateRequest", Map.of("body", "방명록 본문 (10~500자)")),
            Map.entry("GuestbookSummaryDto", Map.of(
                    "writerId", "작성자 유저 ID",
                    "writer", "작성자 닉네임",
                    "body", "본문",
                    "createdAt", "작성 시각"
            )),
            Map.entry("GuestbookPageResponse", Map.of(
                    "guestbooks", "방명록 목록",
                    "page", "현재 페이지 번호",
                    "size", "페이지 크기",
                    "totalElements", "전체 항목 수",
                    "totalPages", "전체 페이지 수",
                    "hasNext", "다음 페이지 존재 여부"
            )),
            Map.entry("ProfileGuestbookCreateRequest", Map.of("body", "방명록 본문")),

            Map.entry("ReservationCreateRequest", Map.of(
                    "guesthouseId", "숙소 ID",
                    "startDate", "체크인 날짜 (오늘 이후)",
                    "endDate", "체크아웃 날짜",
                    "headcount", "투숙 인원 (1명 이상)"
            )),
            Map.entry("ReservationDetailResponse", Map.ofEntries(
                    Map.entry("reservationId", "예약 ID"),
                    Map.entry("guesthouseId", "숙소 ID"),
                    Map.entry("guesthouseName", "숙소 이름"),
                    Map.entry("startDate", "체크인 날짜"),
                    Map.entry("endDate", "체크아웃 날짜"),
                    Map.entry("headcount", "인원수"),
                    Map.entry("cost", "결제한 도토리 금액"),
                    Map.entry("address", "숙소 주소"),
                    Map.entry("capacity", "숙소 수용 인원"),
                    Map.entry("parking", "주차 제공 여부"),
                    Map.entry("wifi", "와이파이 제공 여부"),
                    Map.entry("breakfast", "조식 제공 여부")
            )),
            Map.entry("ReservationSummaryResponse", Map.of(
                    "reservationId", "예약 ID",
                    "guesthouseId", "숙소 ID",
                    "guesthouseName", "숙소 이름",
                    "startDate", "체크인 날짜",
                    "endDate", "체크아웃 날짜",
                    "headcount", "인원수"
            )),

            Map.entry("AcornChargeRequest", Map.of("amount", "증감량. 양수=충전, 음수=차감 (0은 불가)")),
            Map.entry("AcornChargeResponse", Map.of("acorns", "증감 반영 후 도토리 잔액")),
            Map.entry("GamePlayRequest", Map.of("winAmount", "이번 판 획득량 (꽝이면 0)")),
            Map.entry("GamePlayResponse", Map.of("acorns", "참여비 차감 + 획득분 반영 후 도토리 잔액")),
            Map.entry("AcornMeResponse", Map.of(
                    "balance", "현재 도토리 잔액",
                    "playCount", "오늘 게임 참여 횟수",
                    "dailyLimit", "하루 참여 제한"
            )),
            Map.entry("AcornHistoryResponse", Map.of(
                    "history", "내역 목록",
                    "page", "현재 페이지 번호",
                    "size", "페이지 크기",
                    "totalElements", "전체 내역 수",
                    "totalPages", "전체 페이지 수",
                    "hasNext", "다음 페이지 존재 여부"
            )),
            Map.entry("Item", Map.of(
                    "reason", "사유 (GAME_ENTRY, GAME_WIN, RESERVATION, RESERVATION_CANCEL, CHARGE)",
                    "amount", "증감량 (부호 있음)",
                    "balance", "이 거래 직후 잔액",
                    "createdAt", "거래 일시"
            )),

            Map.entry("FriendAddRequest", Map.of("targetUserId", "일촌으로 추가할 대상 유저 ID")),
            Map.entry("FriendDto", Map.of(
                    "userId", "일촌 유저 ID",
                    "nickname", "일촌 닉네임",
                    "since", "일촌 맺은 일시"
            )),
            Map.entry("DegreeResponse", Map.of(
                    "targetUserId", "대상 유저 ID",
                    "reachable", "촌수 계산 가능 여부",
                    "degree", "촌수 (직접 일촌이면 1)",
                    "path", "나 → 대상까지의 경로"
            )),
            Map.entry("PathNode", Map.of(
                    "userId", "경로상 유저 ID",
                    "nickname", "경로상 유저 닉네임"
            )),

            Map.entry("MusicCreateRequest", Map.of("title", "곡 제목", "artist", "아티스트")),
            Map.entry("MusicDto", Map.of(
                    "id", "음악 카탈로그 ID",
                    "title", "곡 제목",
                    "artist", "아티스트"
            )),

            Map.entry("VisitResponse", Map.of(
                    "counted", "이번 방문으로 투데이가 반영됐는지 여부",
                    "visitorCount", "반영 후 홈피 주인의 누적 방문자 수"
            )),
            Map.entry("FootprintDto", Map.of(
                    "visitorId", "방문자 유저 ID",
                    "nickname", "방문자 닉네임",
                    "visitedAt", "가장 최근 방문 시각"
            )),

            Map.entry("WaveRequest", Map.of("targetUserId", "놀러갈 대상 유저 ID")),
            Map.entry("WaveResponse", Map.of(
                    "targetUserId", "놀러간 대상 유저 ID",
                    "rewardedAcorns", "이번 방문으로 받은 도토리 (첫 파도타기가 아니면 0)",
                    "acornBalance", "지급 반영 후 내 도토리 잔액",
                    "wavesToday", "오늘 파도탄 횟수 (이번 것 포함)"
            )),
            Map.entry("WaveMeResponse", Map.of(
                    "wavesToday", "오늘 파도탄 횟수",
                    "rewardClaimed", "오늘 첫 파도타기 보상을 받았는지",
                    "dailyLimit", "하루 파도타기 제한"
            )),
            Map.entry("RecommendationResponse", Map.of("recommendations", "추천 카드 목록")),
            Map.entry("RecommendedUser", Map.ofEntries(
                    Map.entry("userId", "유저 ID"),
                    Map.entry("nickname", "닉네임"),
                    Map.entry("visitorCount", "미니홈피 방문자 수"),
                    Map.entry("friendCount", "일촌 수"),
                    Map.entry("memberSince", "가입 일시"),
                    Map.entry("degreeFromMe", "나로부터의 촌수"),
                    Map.entry("reasons", "추천 사유 목록")
            )),
            Map.entry("Reason", Map.of(
                    "type", "사유 타입 (CO_VISITOR, MUTUAL_FRIEND, CO_REGION, RANDOM)",
                    "label", "사유 문구"
            ))
    );

    private static void enrichSchemas(OpenAPI openApi) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
            return;
        }
        openApi.getComponents().getSchemas().forEach((schemaName, schema) -> {
            Map<String, String> fields = SCHEMA_FIELD_DESCRIPTIONS.get(schemaName);
            if (fields == null || schema.getProperties() == null) {
                return;
            }
            schema.getProperties().forEach((propName, propSchema) -> {
                String description = fields.get(propName);
                if (description != null && propSchema instanceof Schema<?> s) {
                    s.setDescription(description);
                }
            });
        });
    }
}
