package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.CoachProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoachRepository extends JpaRepository<CoachProfile, Long>,
        JpaSpecificationExecutor<CoachProfile> {
    List<CoachProfile> findTop6ByOrderByRatingDesc();

    List<CoachProfile> findTop6ByOrderByStudentsDesc();
}
