package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.GymCoach;
import com.minhthien.web.coach.enums.GymCoachStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GymCoachRepository extends JpaRepository<GymCoach, Long> {
    @EntityGraph(attributePaths = {"gym", "gym.owner", "coach", "coach.user", "coach.category"})
    Optional<GymCoach> findFirstByCoachIdAndStatus(Long coachId, GymCoachStatus status);

    @EntityGraph(attributePaths = {"gym", "gym.owner", "coach", "coach.user", "coach.category"})
    List<GymCoach> findByGymIdAndStatusOrderByJoinedAtDesc(Long gymId, GymCoachStatus status);

    @EntityGraph(attributePaths = {"gym", "gym.owner", "coach", "coach.user", "coach.category"})
    Optional<GymCoach> findByGymIdAndCoachIdAndStatus(Long gymId, Long coachId, GymCoachStatus status);

    boolean existsByCoachIdAndStatus(Long coachId, GymCoachStatus status);
}
