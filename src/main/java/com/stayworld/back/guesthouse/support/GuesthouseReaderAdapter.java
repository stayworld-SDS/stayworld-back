package com.stayworld.back.guesthouse.support;

import com.stayworld.back.global.exception.NotFoundException;
import com.stayworld.back.guesthouse.entity.Guesthouse;
import com.stayworld.back.guesthouse.repository.GuesthouseRepository;
import com.stayworld.back.reservation.support.GuesthouseInfo;
import com.stayworld.back.reservation.support.GuesthouseReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * reservation 도메인이 정의한 {@link GuesthouseReader} 포트의 실제 구현.
 * guesthouse 도메인의 {@link GuesthouseRepository} 를 읽어 {@link GuesthouseInfo} 로 변환한다.
 */
@Component
@RequiredArgsConstructor
public class GuesthouseReaderAdapter implements GuesthouseReader {

    private final GuesthouseRepository guesthouseRepository;

    @Override
    public GuesthouseInfo read(Long guesthouseId) {
        return guesthouseRepository.findById(guesthouseId)
                .map(this::toInfo)
                .orElseThrow(() -> new NotFoundException("게스트하우스를 찾을 수 없습니다. id=" + guesthouseId));
    }

    @Override
    public Map<Long, GuesthouseInfo> readAll(Collection<Long> guesthouseIds) {
        return guesthouseRepository.findAllById(guesthouseIds).stream()
                .collect(Collectors.toMap(Guesthouse::getId, this::toInfo));
    }

    private GuesthouseInfo toInfo(Guesthouse guesthouse) {
        return new GuesthouseInfo(
                guesthouse.getId(),
                guesthouse.getName(),
                guesthouse.getPrice(),
                guesthouse.getAddress(),
                guesthouse.getCapacity(),
                guesthouse.isParking(),
                guesthouse.isWifi(),
                guesthouse.isBreakfast()
        );
    }
}
