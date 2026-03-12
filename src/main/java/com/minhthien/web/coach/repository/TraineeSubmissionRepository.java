package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.TraineeSubmission;
import com.minhthien.web.coach.enums.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TraineeSubmissionRepository
        extends JpaRepository<TraineeSubmission, Long> {

    List<TraineeSubmission> findByCoachId(Long coachId);

    List<TraineeSubmission> findByTraineeId(Long traineeId);
    List<TraineeSubmission> findByCoachVideoId(Long coachVideoId);
    long countByCoachId(Long coachId);

    long countByCoachIdAndStatus(Long coachId, SubmissionStatus status);

}
