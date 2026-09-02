package com.stayworld.back.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stayworld.back.friend.service.FriendService;
import com.stayworld.back.profile.service.ProfileGuestbookService;
import com.stayworld.back.profile.service.ProfileVisitService;
import com.stayworld.back.user.dto.CreateDto;
import com.stayworld.back.user.dto.DeleteDto;
import com.stayworld.back.user.dto.ModifyDto;
import com.stayworld.back.user.repository.UserRepository;
import com.stayworld.back.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    FriendService friendService;

    @MockitoBean
    ProfileGuestbookService profileGuestbookService;

    @MockitoBean
    ProfileVisitService profileVisitService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String body(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    @Test
    void createUser_userService에서_예외_던지면_400() throws Exception {
        when(userService.createUser(any(CreateDto.class))).thenThrow(
            new IllegalArgumentException()
        );

        CreateDto createDto = new CreateDto();
        createDto.setPassword("1q2w3e4r");
        createDto.setEmail("");
        createDto.setNickname("김도영");
        createDto.setPhoneNumber("010-1234-5678");

        mockMvc
            .perform(
                post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body(createDto))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createUser_유효하지_않은_이메일_형식이면_400() throws Exception {
        CreateDto createDto = new CreateDto();
        createDto.setPassword("1q2w3e4r");
        createDto.setEmail("invalidEmailWithoutAt");
        createDto.setNickname("김도영");
        createDto.setPhoneNumber("010-1234-5678");

        mockMvc
            .perform(
                post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body(createDto))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getUserDetailsBySession_세션_값_존재하지_않으면_401()
        throws Exception {
        mockMvc
            .perform(get("/users/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    void checkEmailOccupancy_존재하는_이메일일시_true() throws Exception {
        when(userService.checkEmailOccupancy("foo@bar.com")).thenReturn(true);

        mockMvc
            .perform(get("/users/check-email/foo@bar.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void modifyUserDetails_세션_값_존재하지_않으면_401() throws Exception {
        ModifyDto modifyDto = new ModifyDto();
        modifyDto.setNickname("김도영");
        modifyDto.setPhoneNumber("010-1234-5678");

        mockMvc
            .perform(
                patch("/users/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body(modifyDto))
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    void deleteUser_세션_값_존재하지_않으면_401() throws Exception {
        DeleteDto deleteDto = new DeleteDto();
        deleteDto.setPassword("1q2w3e4r");

        mockMvc
            .perform(
                delete("/users/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body(deleteDto))
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }
}
