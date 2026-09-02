package com.stayworld.back.user.controller;

import com.stayworld.back.friend.dto.FriendDto;
import com.stayworld.back.friend.service.FriendService;
import com.stayworld.back.global.exception.UnauthorizedException;
import com.stayworld.back.global.response.ApiResponse;
import com.stayworld.back.guesthouse.dto.GuestbookPageResponse;
import com.stayworld.back.profile.dto.FootprintDto;
import com.stayworld.back.profile.dto.ProfileGuestbookCreateRequest;
import com.stayworld.back.profile.dto.VisitResponse;
import com.stayworld.back.profile.service.ProfileGuestbookService;
import com.stayworld.back.profile.service.ProfileVisitService;
import com.stayworld.back.user.dto.CreateDto;
import com.stayworld.back.user.dto.DeleteDto;
import com.stayworld.back.user.dto.ModifyDto;
import com.stayworld.back.user.dto.ProfileMusicAddRequest;
import com.stayworld.back.user.dto.ProfileMusicDto;
import com.stayworld.back.user.dto.ProfilePictureDto;
import com.stayworld.back.user.dto.PublicStatsDto;
import com.stayworld.back.user.dto.PublicUserDto;
import com.stayworld.back.user.dto.UserDto;
import com.stayworld.back.user.dto.UserSearchDto;
import com.stayworld.back.user.service.ProfileMusicService;
import com.stayworld.back.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    public static final String SESSION_MEMBER_ID = "MEMBER_ID";

    @Autowired
    private final UserService userService;

    private final FriendService friendService;
    private final ProfileGuestbookService profileGuestbookService;
    private final ProfileVisitService profileVisitService;
    private final ProfileMusicService profileMusicService;

    @PostMapping("/users")
    public ApiResponse<UserDto> createUser(@Valid @RequestBody CreateDto dto) {
        return ApiResponse.success(
            "유저 생성에 성공하였습니다.",
            userService.createUser(dto)
        );
    }

    // 타인의 미니홈피 프로필 (공개 정보만). 본인 전체 정보는 GET /users/me.
    @GetMapping("/users/{userId}")
    public ApiResponse<PublicUserDto> getUserDetailsById(
        @PathVariable("userId") long userId
    ) {
        return ApiResponse.success(userService.getPublicProfile(userId));
    }

    @GetMapping("/users/me")
    public ApiResponse<UserDto> getUserDetailsBySession(HttpSession session) {
        return ApiResponse.success(
            userService.getUserById(getLoginMemberId(session))
        );
    }

    @GetMapping("/users/search")
    public ApiResponse<List<UserSearchDto>> searchUsers(
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(userService.searchByNickname(keyword));
    }

    @GetMapping("/users/{userId}/public-stats")
    public ApiResponse<PublicStatsDto> getPublicStats(
        @PathVariable("userId") long userId
    ) {
        return ApiResponse.success(userService.getPublicStats(userId));
    }

    @GetMapping("/users/{userId}/profile-picture")
    public ApiResponse<ProfilePictureDto> getProfilePictureId(
        @PathVariable("userId") long userId
    ) {
        return ApiResponse.success(userService.getProfilePictureId(userId));
    }

    @PatchMapping("/users/{userId}/profile-picture")
    public ApiResponse<Void> modifyProfilePictureId(
        @PathVariable("userId") long userId,
        @RequestBody ProfilePictureDto dto
    ) {
        userService.modifyProfilePictureId(userId, dto.getProfilePictureId());
        return ApiResponse.success(null);
    }

    @GetMapping("/users/{userId}/friends")
    public ApiResponse<List<FriendDto>> getFriendsOf(
        @PathVariable("userId") long userId
    ) {
        return ApiResponse.success(friendService.getFriendsOf(userId));
    }

    // 미니홈피 진입 시 호출. 본인/오늘 재방문이면 투데이는 안 오르고 counted=false.
    @PostMapping("/users/{userId}/visits")
    public ApiResponse<VisitResponse> recordVisit(
            @PathVariable("userId") long userId,
            HttpSession session) {
        return ApiResponse.success(profileVisitService.recordVisit(userId, getLoginMemberId(session)));
    }

    // 미니홈피에 남은 발자국 (방문자별 최근 방문, 최대 20개).
    @GetMapping("/users/{userId}/footprints")
    public ApiResponse<List<FootprintDto>> getFootprints(@PathVariable("userId") long userId) {
        return ApiResponse.success(profileVisitService.footprints(userId));
    }

    @GetMapping("/users/{userId}/guestbooks")
    public ApiResponse<GuestbookPageResponse> getProfileGuestbooks(
        @PathVariable("userId") long userId,
        @RequestParam(defaultValue = "0") int page
    ) {
        return ApiResponse.success(
            profileGuestbookService.findByOwnerId(userId, page)
        );
    }

    @PostMapping("/users/{userId}/guestbooks")
    public ApiResponse<Void> postProfileGuestbook(
        @PathVariable("userId") long userId,
        @RequestParam Long writerId,
        @Valid @RequestBody ProfileGuestbookCreateRequest request
    ) {
        profileGuestbookService.write(userId, writerId, request);
        return ApiResponse.success(null);
    }

    @GetMapping("/users/{userId}/musics")
    public ApiResponse<List<ProfileMusicDto>> getProfileMusics(
        @PathVariable("userId") long userId
    ) {
        return ApiResponse.success(profileMusicService.getPlaylist(userId));
    }

    @PostMapping("/users/me/musics")
    public ApiResponse<ProfileMusicDto> addProfileMusic(
        @Valid @RequestBody ProfileMusicAddRequest request,
        HttpSession session
    ) {
        return ApiResponse.success(
            profileMusicService.addToPlaylist(getLoginMemberId(session), request)
        );
    }

    @GetMapping("/users/check-email/{email}")
    public ApiResponse<Boolean> checkEmailOccupancy(
        @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        ) @PathVariable("email") String email
    ) {
        return ApiResponse.success(userService.checkEmailOccupancy(email));
    }

    @PatchMapping("/users/me")
    public ApiResponse<UserDto> modifyUserDetails(
        @RequestBody ModifyDto dto,
        HttpSession session
    ) {
        return ApiResponse.success(
            userService.modifyUserDetails(getLoginMemberId(session), dto)
        );
    }

    @DeleteMapping("/users/me")
    public ApiResponse<Void> deleteUser(
        @RequestBody DeleteDto dto,
        HttpSession session
    ) {
        userService.deleteUser(getLoginMemberId(session), dto);
        session.invalidate();
        return ApiResponse.success("회원 탈퇴가 완료되었습니다.", null);
    }

    private long getLoginMemberId(HttpSession session) {
        Object memberId = session.getAttribute(SESSION_MEMBER_ID);
        if (memberId == null) {
            throw new UnauthorizedException();
        }
        return (long) memberId;
    }
}
