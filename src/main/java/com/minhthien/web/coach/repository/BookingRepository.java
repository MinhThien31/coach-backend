package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.Booking;
import com.minhthien.web.coach.entity.TraineeProfile;
import com.minhthien.web.coach.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByTraineeId(Long traineeId);

    boolean existsByCoachIdAndDayOfWeekAndStartTimeAndStatusNot(
            Long coachId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            BookingStatus status
    );

    @Query("""
    SELECT COUNT(DISTINCT b.trainee.id)
    FROM Booking b
    WHERE b.coach.id = :coachId
    """)
    Long countStudentsByCoach(Long coachId);

    @Query("""
    SELECT COUNT(b.id)
    FROM Booking b
    WHERE b.coach.id = :coachId
    """)
    Long countSessionsByCoach(Long coachId);

    @Query("""
    SELECT COUNT(b)
    FROM Booking b
    WHERE b.trainee.id = :traineeId
    AND MONTH(b.startDate) = :month
    AND YEAR(b.startDate) = :year
    """)
    Long countSessionsThisMonth(Long traineeId, int month, int year);

    @Query("""
    SELECT SUM(b.price)
    FROM Booking b
    WHERE b.trainee.id = :traineeId
    AND MONTH(b.startDate) = :month
    AND YEAR(b.startDate) = :year
    """)
    Double sumMonthlySpending(Long traineeId, int month, int year);

    List<Booking> findByTraineeIdAndStartDateBetween(
            Long traineeId,
            LocalDate start,
            LocalDate end
    );

    List<Booking> findByTraineeIdAndStartDate(
            Long traineeId,
            LocalDate date
    );

    List<Booking> findTop3ByTraineeIdAndStartDateAfterOrderByStartDateAsc(
            Long traineeId,
            LocalDate now
    );

    @Query("""
SELECT DISTINCT tp
FROM Booking b
JOIN TraineeProfile tp ON tp.user.id = b.trainee.id
WHERE b.coach.id = :coachId
AND LOWER(tp.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
    List<TraineeProfile> searchBookedTrainees(Long coachId, String keyword);
}