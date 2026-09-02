package com.stayworld.back.friend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FriendAddRequest {
    @NotNull
    Long targetUserId;
}
