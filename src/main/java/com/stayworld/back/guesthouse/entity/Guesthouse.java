package com.stayworld.back.guesthouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
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
    private boolean parking;
    @Column(nullable = false)
    private boolean wifi;
    @Column(nullable = false)
    private boolean breakfast;
    @Column(length = 500)
    private String introduction;
    @Column(nullable = false)
    private int visitorCount;
    @Column(length = 80)
    private String music;
}
