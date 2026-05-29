package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.CoachSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<CoachSchedule, Long> {
    List<CoachSchedule> findByCoachId(Long coachId);

    @Query("""
    SELECT COUNT(s) > 0
    FROM CoachSchedule s
    WHERE s.coach.id = :coachId
    AND s.dayOfWeek = :dayOfWeek
    AND s.startTime = :startTime
    AND s.endTime = :endTime
    AND s.startDate <= :startDate
    AND s.endDate >= :endDate
    """)
    boolean existsBookableSlot(
            Long coachId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            LocalDate startDate,
            LocalDate endDate
    );
}
