package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByTraineeId(Long traineeId);
    boolean existsByCoachIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            Long coachId,
            LocalDateTime end,
            LocalDateTime start
    );

    @Query("SELECT COUNT(DISTINCT b.trainee.id) FROM Booking b WHERE b.coach.id = :coachId")
    Long countStudentsByCoach(Long coachId);

    @Query("SELECT COUNT(b.id) FROM Booking b WHERE b.coach.id = :coachId")
    Long countSessionsByCoach(Long coachId);
}
