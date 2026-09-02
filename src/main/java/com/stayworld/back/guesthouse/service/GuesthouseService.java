package com.stayworld.back.guesthouse.service;

import com.stayworld.back.guesthouse.dto.GuesthouseDto;
import com.stayworld.back.guesthouse.entity.Guesthouse;
import com.stayworld.back.guesthouse.exception.GuesthouseNotFoundException;
import com.stayworld.back.guesthouse.repository.GuesthouseRepository;
import com.stayworld.back.reservation.repository.DailyOccupancyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GuesthouseService {

    private final DailyOccupancyRepository dailyOccupancyRepository;
    private final GuesthouseRepository guesthouseRepository;

    public List<GuesthouseDto> searchAvailableGuesthouses(
            String location,
            LocalDate start,
            LocalDate end,
            int headCount
    ){

        if (start.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("지난 날짜는 검색할 수 없습니다.");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("체크아웃은 체크인보다 뒤여야 합니다.");
        }
        if (headCount < 1) {
            throw new IllegalArgumentException("인원수는 1명 이상이어야 합니다.");
        }

        String escapedLocation = location == null ? "" : location.trim()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");

        List<Guesthouse> guesthouse = dailyOccupancyRepository.findGuesthouseByLocationAndAvailability(
                escapedLocation,
                start,
                end,
                headCount
        );

        return guesthouse.stream().map(GuesthouseDto::from).toList();
    }

    public GuesthouseDto findById(long id){
        Guesthouse guesthouse = guesthouseRepository
                .findById(id)
                .orElseThrow(GuesthouseNotFoundException::new);

        GuesthouseDto guesthouseDto = new GuesthouseDto();
        guesthouseDto.setAddress(guesthouse.getAddress());
        guesthouseDto.setBreakfastProvided(guesthouse.isBreakfastProvided());
        guesthouseDto.setCapacity(guesthouse.getCapacity());
        guesthouseDto.setId(guesthouse.getId());
        guesthouseDto.setIntroduction(guesthouse.getIntroduction());
        guesthouseDto.setName(guesthouse.getName());
        guesthouseDto.setParkingProvided(guesthouse.isParkingProvided());
        guesthouseDto.setPhoneNumber(guesthouse.getPhoneNumber());
        guesthouseDto.setPrice(guesthouse.getPrice());
        guesthouseDto.setVisitorCount(guesthouse.getVisitorCount());
        guesthouseDto.setWifiProvided(guesthouse.isWifiProvided());
        guesthouseDto.setMusic(guesthouse.getMusic());

        return guesthouseDto;
    }
}
