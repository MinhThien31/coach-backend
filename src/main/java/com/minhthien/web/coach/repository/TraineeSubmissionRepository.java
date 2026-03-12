package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.TraineeSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TraineeSubmissionRepository
        extends JpaRepository<TraineeSubmission, Long> {

    List<TraineeSubmission> findByCoachId(Long coachId);

    List<TraineeSubmission> findByTraineeId(Long traineeId);

}
