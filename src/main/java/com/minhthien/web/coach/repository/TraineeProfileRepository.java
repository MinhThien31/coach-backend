package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.TraineeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TraineeProfileRepository extends JpaRepository<TraineeProfile, Long> {

    Optional<TraineeProfile> findByUserId(Long userId);

    List<TraineeProfile> findByCoachId(Long coachId);

    List<TraineeProfile> findByUser_FullNameContainingIgnoreCase(String keyword);

}
