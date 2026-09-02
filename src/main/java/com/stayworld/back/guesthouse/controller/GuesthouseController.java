package com.stayworld.back.guesthouse.controller;

import com.stayworld.back.global.response.ApiResponse;
import com.stayworld.back.global.auth.LoginMember;
import com.stayworld.back.guesthouse.dto.GuestbookCreateRequest;
import com.stayworld.back.guesthouse.dto.GuestbookPageResponse;
import com.stayworld.back.guesthouse.dto.GuesthouseDto;
import com.stayworld.back.guesthouse.service.GuestbookService;
import com.stayworld.back.guesthouse.service.GuesthouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/guesthouses")
public class GuesthouseController {

    private final GuestbookService guestbookService;
    private final GuesthouseService guesthouseService;

    @GetMapping
    public ApiResponse<List<GuesthouseDto>> searchAvailableGuesthouses(
            @RequestParam String location,
            @RequestParam("start") LocalDate startDate,
            @RequestParam("end") LocalDate endDate,
            @RequestParam("headCount") int headcount
            ){
        return ApiResponse.success(guesthouseService.searchAvailableGuesthouses(
                location, startDate, endDate, headcount
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<GuesthouseDto> getGuesthouse(
            @PathVariable("id") long id
    ){
        return ApiResponse.success(guesthouseService.findById(id));
    }

    @GetMapping("/{id}/guestbooks")
    public ApiResponse<GuestbookPageResponse> getGuestbook(
            @PathVariable("id") long id,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ApiResponse.success(guestbookService.findByGuesthouseId(id, page));
    }

    @PostMapping("/{id}/guestbooks")
    public ApiResponse<Void> postGuestbook(
            @LoginMember Long userId,
            @PathVariable("id") long guesthouseId,
            @Valid @RequestBody GuestbookCreateRequest guestbook
    ){
        guestbookService.saveGuestbook(userId, guesthouseId, guestbook);
        return ApiResponse.success(null);
    }

}
