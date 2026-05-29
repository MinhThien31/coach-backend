package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.TraineeBodyMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TraineeBodyMetricRepository extends JpaRepository<TraineeBodyMetric, Long> {

    List<TraineeBodyMetric> findByTraineeIdOrderByMeasuredAtAsc(Long traineeId);

    List<TraineeBodyMetric> findByTraineeIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
            Long traineeId,
            LocalDate start,
            LocalDate end
    );
}
