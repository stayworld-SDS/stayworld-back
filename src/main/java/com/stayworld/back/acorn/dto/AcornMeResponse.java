package com.stayworld.back.acorn.dto;

/** GET /acorns/me 응답. */
public record AcornMeResponse(
        int balance,
        boolean participated
) {
}
