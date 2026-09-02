package com.stayworld.back.profile.repository;

import com.stayworld.back.profile.entity.ProfileVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProfileVisitRepository extends JpaRepository<ProfileVisit, Long> {

    /** 그 사람이 그 홈피를 오늘 이미 다녀갔는지 (투데이 중복 카운트 방지). */
    boolean existsByOwnerIdAndVisitorIdAndVisitDate(Long ownerId, Long visitorId, LocalDate visitDate);

    /** 최근 방문 행 (발자국 목록 재료). 방문자별 dedup 은 서비스에서. */
    List<ProfileVisit> findTop50ByOwnerIdOrderByIdDesc(Long ownerId);
}
