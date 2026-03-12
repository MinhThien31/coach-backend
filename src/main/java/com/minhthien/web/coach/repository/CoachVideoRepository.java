package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.CoachVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoachVideoRepository extends JpaRepository<CoachVideo, Long> {

    List<CoachVideo> findByCoachId(Long coachId);

}