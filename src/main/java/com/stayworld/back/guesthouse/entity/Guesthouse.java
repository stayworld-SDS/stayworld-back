package com.stayworld.back.guesthouse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ⚠️ 껍데기 - guesthouse 도메인 담당자가 작업 중이라고 가정한 임시 엔티티.
 * reservation 도메인이 {@code @ManyToOne} 으로 참조하고, 상세/목록 응답에
 * 필요한 최소 필드만 정의했다. 실제 구현으로 교체될 때 필드명/타입이 바뀌면
 * reservation 쪽 매핑도 같이 맞춰야 한다.
 */
@Entity
@Table(name = "guesthouses")
@Getter
@NoArgsConstructor
public class Guesthouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;          // 1박 요금

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private int capacity;       // 최대 수용 인원

    @Column(nullable = false)
    private boolean parking;

    @Column(nullable = false)
    private boolean wifi;

    @Column(nullable = false)
    private boolean breakfast;
}
