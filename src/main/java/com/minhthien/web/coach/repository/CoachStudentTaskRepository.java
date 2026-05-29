package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.CoachStudentTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoachStudentTaskRepository extends JpaRepository<CoachStudentTask, Long> {
    List<CoachStudentTask> findByCoachIdAndTraineeIdOrderByCreatedAtDesc(Long coachId, Long traineeId);
}
