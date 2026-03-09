package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.CoachCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoachCertificateRepository extends JpaRepository<CoachCertificate, Long> {

    List<CoachCertificate> findByCoachId(Long coachId);

}
