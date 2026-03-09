package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.CoachSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<CoachSchedule, Long> {
    List<CoachSchedule> findByCoachId(Long coachId);
}
