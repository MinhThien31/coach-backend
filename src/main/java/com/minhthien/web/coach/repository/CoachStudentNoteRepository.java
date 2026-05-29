package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.CoachStudentNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoachStudentNoteRepository extends JpaRepository<CoachStudentNote, Long> {
    List<CoachStudentNote> findByCoachIdAndTraineeIdOrderByCreatedAtDesc(Long coachId, Long traineeId);
}
