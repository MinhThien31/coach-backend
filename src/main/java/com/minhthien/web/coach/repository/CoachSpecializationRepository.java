package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.CoachSpecialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoachSpecializationRepository extends JpaRepository<CoachSpecialization, Long> {

    List<CoachSpecialization> findByCoachId(Long coachId);

}
