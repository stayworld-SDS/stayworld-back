package com.stayworld.back.guesthouse.entity;

import com.stayworld.back.guesthouse.support.RegionExtractor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "guesthouses", indexes = @Index(name = "idx_guesthouse_region", columnList = "region"))
public class Guesthouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 50, nullable = false)
    private String name;
    @Column(nullable = false)
    private int price;
    @Column(length = 20, nullable = false)
    private String phoneNumber;
    @Column(length = 100, nullable = false)
    private String address;
    @Column(nullable = false)
    private int capacity;
    @Column(nullable = false)
    private boolean parkingProvided;
    @Column(nullable = false)
    private boolean wifiProvided;
    @Column(nullable = false)
    private boolean breakfastProvided;
    @Column(length = 500)
    private String introduction;
    @Column(nullable = false)
    private int visitorCount;
    @Column(length = 80)
    private String music;

    /**
     * 주소에서 뽑은 광역 지역명 (추천의 "같은 지역" 매칭 키). 저장 시 {@code address} 로부터 자동 채움.
     * 수기 seed INSERT 가 값을 안 넣어도 되도록 DB default 는 빈 문자열이고,
     * {@code GuesthouseRegionBackfill} 이 기동 시 빈 값을 메운다.
     */
    @Column(length = 20, nullable = false, columnDefinition = "varchar(20) default ''")
    private String region;

    @PrePersist
    @PreUpdate
    void deriveRegion() {
        if (region == null || region.isBlank()) {
            region = RegionExtractor.from(address);
        }
    }
}
