package com.stayworld.back.guesthouse.entity;

import com.stayworld.back.user.entity.User;
import com.stayworld.back.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="guestbooks")
public class Guestbook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guesthouse_id", nullable = false)
    private Guesthouse guesthouse;
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
//    private Reservation reservation;
    @Column(length = 500, nullable = false)
    String body;
    @Column(nullable = false)
    LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
