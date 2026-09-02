package com.stayworld.back.profile.service;

import com.stayworld.back.global.exception.NotFoundException;
import com.stayworld.back.profile.dto.FootprintDto;
import com.stayworld.back.profile.dto.VisitResponse;
import com.stayworld.back.profile.entity.ProfileVisit;
import com.stayworld.back.profile.repository.ProfileVisitRepository;
import com.stayworld.back.user.entity.User;
import com.stayworld.back.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileVisitServiceTest {

    @Mock
    ProfileVisitRepository profileVisitRepository;
    @Mock
    UserRepository userRepository;
    @InjectMocks
    ProfileVisitService profileVisitService;

    @Test
    void 첫_방문이면_발자국을_남기고_투데이가_오른다() {
        User owner = user(1L, "주인");
        owner.setVisitorCount(7);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(profileVisitRepository.existsByOwnerIdAndVisitorIdAndVisitDate(eq(1L), eq(2L), any()))
                .thenReturn(false);

        VisitResponse res = profileVisitService.recordVisit(1L, 2L);

        assertThat(res.counted()).isTrue();
        assertThat(res.visitorCount()).isEqualTo(8);
        assertThat(owner.getVisitorCount()).isEqualTo(8);
        verify(profileVisitRepository).save(any(ProfileVisit.class));
    }

    @Test
    void 본인_방문은_세지_않는다() {
        User owner = user(1L, "주인");
        owner.setVisitorCount(7);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        VisitResponse res = profileVisitService.recordVisit(1L, 1L);

        assertThat(res.counted()).isFalse();
        assertThat(res.visitorCount()).isEqualTo(7);
        verify(profileVisitRepository, never()).save(any());
    }

    @Test
    void 오늘_이미_다녀갔으면_투데이가_안_오른다() {
        User owner = user(1L, "주인");
        owner.setVisitorCount(7);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(profileVisitRepository.existsByOwnerIdAndVisitorIdAndVisitDate(eq(1L), eq(2L), any()))
                .thenReturn(true);

        VisitResponse res = profileVisitService.recordVisit(1L, 2L);

        assertThat(res.counted()).isFalse();
        assertThat(res.visitorCount()).isEqualTo(7);
        verify(profileVisitRepository, never()).save(any());
    }

    @Test
    void 없는_홈피를_방문하면_404() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileVisitService.recordVisit(99L, 2L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 발자국은_방문자별_최근_방문만_남긴다() {
        when(userRepository.existsById(1L)).thenReturn(true);
        LocalDate d = LocalDate.of(2026, 9, 2);
        when(profileVisitRepository.findTop50ByOwnerIdOrderByIdDesc(1L)).thenReturn(List.of(
                new ProfileVisit(1L, 20L, d),   // 최신
                new ProfileVisit(1L, 30L, d),
                new ProfileVisit(1L, 20L, d),   // 20L 의 예전 방문 → 무시
                new ProfileVisit(1L, 40L, d)
        ));
        when(userRepository.findAllById(any())).thenReturn(List.of(
                user(20L, "가"), user(30L, "나"), user(40L, "다")
        ));

        List<FootprintDto> footprints = profileVisitService.footprints(1L);

        assertThat(footprints).extracting(FootprintDto::visitorId).containsExactly(20L, 30L, 40L);
        assertThat(footprints).extracting(FootprintDto::nickname).containsExactly("가", "나", "다");
    }

    private static User user(long id, String nickname) {
        User user = new User();
        user.setId(id);
        user.setNickname(nickname);
        return user;
    }
}
