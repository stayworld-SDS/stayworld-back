package com.stayworld.back.guesthouse.support;

import com.stayworld.back.guesthouse.entity.Guesthouse;
import com.stayworld.back.guesthouse.repository.GuesthouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 기동 시 {@code region} 이 비어 있는 게하(수기 seed INSERT 등)를 주소에서 유추해 채운다.
 * JPA 로 생성/수정되는 행은 {@link Guesthouse#deriveRegion()} 이 알아서 채우므로 여기선 안 건드린다.
 */
@Component
@RequiredArgsConstructor
public class GuesthouseRegionBackfill implements ApplicationRunner {

    private final GuesthouseRepository guesthouseRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (Guesthouse guesthouse : guesthouseRepository.findWithoutRegion()) {
            guesthouse.setRegion(RegionExtractor.from(guesthouse.getAddress()));
        }
    }
}
