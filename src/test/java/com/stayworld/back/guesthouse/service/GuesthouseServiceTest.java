package com.stayworld.back.guesthouse.service;

import com.stayworld.back.guesthouse.dto.GuesthouseDto;
import com.stayworld.back.guesthouse.entity.Guesthouse;
import com.stayworld.back.guesthouse.exception.GuesthouseNotFoundException;
import com.stayworld.back.guesthouse.repository.GuesthouseRepository;
import com.stayworld.back.reservation.repository.DailyOccupancyRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuesthouseServiceTest {

    @Mock
    DailyOccupancyRepository dailyOccupancyRepository;
    @Mock
    GuesthouseRepository guesthouseRepository;
    @InjectMocks
    GuesthouseService guesthouseService;

    private Guesthouse guesthouse(long id, String address, int visitorCount) {
        Guesthouse guesthouse = new Guesthouse();
        guesthouse.setId(id);
        guesthouse.setName("게하" + id);
        guesthouse.setPrice(10_000);
        guesthouse.setPhoneNumber("02-1234-5678");
        guesthouse.setAddress(address);
        guesthouse.setCapacity(4);
        guesthouse.setVisitorCount(visitorCount);
        guesthouse.setMusic("music-" + id);
        return guesthouse;
    }

    @Test
    void searchAvailableGuesthouses_검색어를_부분검색_조건으로_전달한다() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(2);
        when(dailyOccupancyRepository.findGuesthouseByLocationAndAvailability("강남", start, end, 2))
                .thenReturn(List.of(guesthouse(1L, "서울시 강남구", 10)));

        List<GuesthouseDto> result = guesthouseService.searchAvailableGuesthouses("강남", start, end, 2);

        assertThat(result).extracting(GuesthouseDto::getAddress)
                .containsExactly("서울시 강남구");
        verify(dailyOccupancyRepository)
                .findGuesthouseByLocationAndAvailability("강남", start, end, 2);
    }

    @Test
    void searchAvailableGuesthouses_검색어의_와일드카드는_일반문자로_전달한다() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(2);

        guesthouseService.searchAvailableGuesthouses("%_\\", start, end, 2);

        verify(dailyOccupancyRepository)
                .findGuesthouseByLocationAndAvailability("\\%\\_\\\\", start, end, 2);
    }

    @Test
    void searchAvailableGuesthouses_빈_지역은_전체지역_검색으로_전달한다() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(2);

        guesthouseService.searchAvailableGuesthouses("   ", start, end, 2);

        verify(dailyOccupancyRepository)
                .findGuesthouseByLocationAndAvailability("", start, end, 2);
    }

    @Test
    void searchAvailableGuesthouses_방문자수가_많은_순서로_반환한다() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(2);
        when(dailyOccupancyRepository.findGuesthouseByLocationAndAvailability("서울", start, end, 2))
                .thenReturn(List.of(
                        guesthouse(2L, "서울", 30),
                        guesthouse(3L, "서울", 20),
                        guesthouse(1L, "서울", 10)));

        List<GuesthouseDto> result = guesthouseService.searchAvailableGuesthouses("서울", start, end, 2);

        assertThat(result).extracting(GuesthouseDto::getVisitorCount)
                .containsExactly(30, 20, 10);
    }

    @Test
    void findById_음악을_포함한_숙소정보를_반환한다() {
        when(guesthouseRepository.findById(1L))
                .thenReturn(Optional.of(guesthouse(1L, "서울", 10)));

        GuesthouseDto result = guesthouseService.findById(1L);

        assertThat(result.getMusic()).isEqualTo("music-1");
    }

    @Test
    void findById_숙소의_모든_기본정보를_반환한다() {
        Guesthouse guesthouse = guesthouse(1L, "서울시 강남구", 10);
        guesthouse.setPrice(25_000);
        guesthouse.setPhoneNumber("02-9876-5432");
        guesthouse.setCapacity(6);
        guesthouse.setParkingProvided(true);
        guesthouse.setWifiProvided(true);
        guesthouse.setBreakfastProvided(true);
        guesthouse.setIntroduction("편안한 숙소입니다.");
        when(guesthouseRepository.findById(1L)).thenReturn(Optional.of(guesthouse));

        GuesthouseDto result = guesthouseService.findById(1L);

        assertThat(result).satisfies(dto -> {
            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getName()).isEqualTo("게하1");
            assertThat(dto.getPrice()).isEqualTo(25_000);
            assertThat(dto.getPhoneNumber()).isEqualTo("02-9876-5432");
            assertThat(dto.getAddress()).isEqualTo("서울시 강남구");
            assertThat(dto.getCapacity()).isEqualTo(6);
            assertThat(dto.isParkingProvided()).isTrue();
            assertThat(dto.isWifiProvided()).isTrue();
            assertThat(dto.isBreakfastProvided()).isTrue();
            assertThat(dto.getIntroduction()).isEqualTo("편안한 숙소입니다.");
            assertThat(dto.getVisitorCount()).isEqualTo(10);
            assertThat(dto.getMusic()).isEqualTo("music-1");
        });
    }

    @Test
    void findById_없는_숙소면_404() {
        when(guesthouseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> guesthouseService.findById(999L))
                .isInstanceOf(GuesthouseNotFoundException.class);
    }

    @Test
    void searchAvailableGuesthouses_검색결과가_없으면_빈_목록을_반환한다() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(2);
        when(dailyOccupancyRepository.findGuesthouseByLocationAndAvailability("제주", start, end, 2))
                .thenReturn(List.of());

        List<GuesthouseDto> result = guesthouseService.searchAvailableGuesthouses("제주", start, end, 2);

        assertThat(result).isEmpty();
    }

    @Test
    void searchAvailableGuesthouses_지난_날짜면_400() {
        LocalDate start = LocalDate.now().minusDays(1);

        assertThatThrownBy(() -> guesthouseService.searchAvailableGuesthouses(
                "서울", start, start.plusDays(2), 2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void searchAvailableGuesthouses_체크인과_체크아웃이_같으면_400() {
        LocalDate date = LocalDate.now().plusDays(1);

        assertThatThrownBy(() -> guesthouseService.searchAvailableGuesthouses("서울", date, date, 2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void searchAvailableGuesthouses_체크아웃이_체크인보다_빠르면_400() {
        LocalDate start = LocalDate.now().plusDays(2);

        assertThatThrownBy(() -> guesthouseService.searchAvailableGuesthouses(
                "서울", start, start.minusDays(1), 2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void searchAvailableGuesthouses_인원이_0명이면_400() {
        LocalDate start = LocalDate.now().plusDays(1);

        assertThatThrownBy(() -> guesthouseService.searchAvailableGuesthouses(
                "서울", start, start.plusDays(2), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void searchAvailableGuesthouses_인원이_1명이면_검색한다() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(2);

        guesthouseService.searchAvailableGuesthouses("서울", start, end, 1);

        verify(dailyOccupancyRepository)
                .findGuesthouseByLocationAndAvailability("서울", start, end, 1);
    }

    @Test
    void searchAvailableGuesthouses_숙박기간이_길어도_검색한다() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusYears(1);

        guesthouseService.searchAvailableGuesthouses("서울", start, end, 1);

        verify(dailyOccupancyRepository)
                .findGuesthouseByLocationAndAvailability("서울", start, end, 1);
    }

    @Test
    void searchAvailableGuesthouses_인원에_최대제한을_두지_않는다() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(2);

        guesthouseService.searchAvailableGuesthouses("서울", start, end, 1_000);

        verify(dailyOccupancyRepository)
                .findGuesthouseByLocationAndAvailability("서울", start, end, 1_000);
    }
}
