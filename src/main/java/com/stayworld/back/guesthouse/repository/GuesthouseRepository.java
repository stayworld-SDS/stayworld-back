package com.stayworld.back.guesthouse.repository;

import com.stayworld.back.guesthouse.entity.Guesthouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuesthouseRepository extends JpaRepository<Guesthouse, Long> {
}
