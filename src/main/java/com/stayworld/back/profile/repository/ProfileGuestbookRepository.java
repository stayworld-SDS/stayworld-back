package com.stayworld.back.profile.repository;

import com.stayworld.back.profile.entity.ProfileGuestbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileGuestbookRepository extends JpaRepository<ProfileGuestbook, Long> {
    Page<ProfileGuestbook> findByOwnerId(Long ownerId, Pageable pageable);
}
