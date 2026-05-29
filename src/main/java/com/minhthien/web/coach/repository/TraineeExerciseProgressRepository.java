package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.TraineeExerciseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TraineeExerciseProgressRepository extends JpaRepository<TraineeExerciseProgress, Long> {

    List<TraineeExerciseProgress> findByTraineeIdOrderByMeasuredAtAsc(Long traineeId);

    List<TraineeExerciseProgress> findByTraineeIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
            Long traineeId,
            LocalDate start,
            LocalDate end
    );
}
