package com.stayworld.back.guesthouse.repository;

import com.stayworld.back.guesthouse.entity.Guestbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestbookRepository extends JpaRepository<Guestbook, Long> {

    @EntityGraph(attributePaths = "user")
    Page<Guestbook> findByGuesthouseId(long guesthouseId, Pageable pageable);
}
