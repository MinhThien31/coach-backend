package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
