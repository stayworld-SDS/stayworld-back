package com.stayworld.back.guesthouse.controller;

import com.stayworld.back.global.response.ApiResponse;
import com.stayworld.back.guesthouse.dto.GuestbookDTO;
import com.stayworld.back.guesthouse.dto.GuesthouseDTO;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/guesthouses")
public class GuesthouseController {

    private final GuestbookService guestbookService;
    private final GuesthouseService guesthouseService;

    @GetMapping
    public ApiResponse<List<GuesthouseDTO>> searchGuesthouses(
            @RequestParam String location,
            @RequestParam("start") LocalDate startDate,
            @RequestParam("end") LocalDate endDate,
            @RequestParam("guestCount") int guestCount
            ){
        return ApiResponse.success(List.of());
    }

    @GetMapping("/{id}")
    public ApiResponse<GuesthouseDTO> getGuesthouse(
            @PathVariable("id") int id
    ){
        return ApiResponse.success(new GuesthouseDTO());
    }

    @GetMapping("/{id}/guestbooks")
    public ApiResponse<List<GuestbookDTO>> getGuestbook(
            @PathVariable("id") int id
    ) {
        return ApiResponse.success(List.of());
    }

    @PostMapping("/{id}/guestbooks")
    public ApiResponse<Void> postGuestbook(
            @PathVariable("id") long guesthouseId,
            @RequestBody GuestbookDto guestbook
    ){
        guestbookService.saveGuestbook(guesthouseId, guestbook);
        return ApiResponse.success(null);
    }

}
