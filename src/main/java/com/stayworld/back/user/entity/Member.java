package com.stayworld.back.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Entity
@Component
@Table(name = "members")
@Getter
@Setter
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Column(length = 20, nullable = false)
    String password;
    @Column(length = 100, nullable = false)
    String email;
    @Column(length = 20, nullable = false)
    String nickname;
    @Column(name="phone_number", columnDefinition = "CHAR(20)", length = 20, nullable = false)
    String phoneNumber;
    @Column(nullable = false)
    int balance;
    @Column(nullable = false)
    LocalDateTime created_at;
    @Column(name="visitor_count", nullable = false)
    int visitorCount;
}
