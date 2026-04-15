package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.Booking;
import com.minhthien.web.coach.entity.TraineeProfile;
import com.minhthien.web.coach.enums.BookingStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"trainee", "coach", "coach.user"})
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

    @EntityGraph(attributePaths = {"trainee", "coach", "coach.user"})
    List<Booking> findByTraineeIdAndStartDateBetween(
            Long traineeId,
            LocalDate start,
            LocalDate end
    );

    @EntityGraph(attributePaths = {"trainee", "coach", "coach.user"})
    List<Booking> findByTraineeIdAndStartDate(
            Long traineeId,
            LocalDate date
    );

    @EntityGraph(attributePaths = {"trainee", "coach", "coach.user"})
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

    @EntityGraph(attributePaths = {"trainee", "coach", "coach.user"})
    List<Booking> findByCoachId(Long coachId);

    @EntityGraph(attributePaths = {"trainee", "coach", "coach.user"})
    List<Booking> findByCoachIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long coachId,
            LocalDate endDate,
            LocalDate startDate);

    @Query("""
SELECT COUNT(b)
FROM Booking b
WHERE b.coach.id = :coachId
AND b.dayOfWeek = :dayOfWeek
AND :today BETWEEN b.startDate AND b.endDate
""")
    long countTodaySessions(Long coachId, DayOfWeek dayOfWeek, LocalDate today);

    @Query("""
SELECT COUNT(b)
FROM Booking b
WHERE b.coach.id = :coachId
AND :startOfWeek <= b.endDate
AND :endOfWeek >= b.startDate
""")
    long countWeekSessions(Long coachId, LocalDate startOfWeek, LocalDate endOfWeek);

    @Query("""
SELECT COALESCE(SUM(b.price),0)
FROM Booking b
WHERE b.coach.id = :coachId
AND :startOfWeek <= b.endDate
AND :endOfWeek >= b.startDate
AND b.status = 'COMPLETED'
""")
    double sumWeekRevenue(Long coachId, LocalDate startOfWeek, LocalDate endOfWeek);

    long countByCoachIdAndStatus(Long coachId, BookingStatus status);
}
