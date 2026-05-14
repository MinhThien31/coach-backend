package com.minhthien.web.coach.service.impl;

import com.minhthien.web.coach.dto.response.CoachDashboardResponse;
import com.minhthien.web.coach.dto.response.DashboardStatsResponse;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.enums.BookingStatus;
import com.minhthien.web.coach.repository.BookingRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow();

        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        long thisMonth = bookingRepository.countSessionsThisMonth(
                user.getId(),
                startOfMonth,
                endOfMonth
        );

        long completed = bookingRepository.countByTraineeIdAndStatus(
                user.getId(),
                BookingStatus.COMPLETED
        );

        long upcoming = bookingRepository.countByTraineeIdAndStatus(
                user.getId(),
                BookingStatus.PENDING
        );

        double spending = bookingRepository.sumMonthlySpending(
                user.getId(),
                startOfMonth,
                endOfMonth
        );

        return DashboardStatsResponse.builder()
                .sessionsThisMonth(thisMonth)
                .completedSessions(completed)
                .upcomingSessions(upcoming)
                .monthlySpending(spending)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CoachDashboardResponse getCoachDashboard() {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User coach = userRepository
                .findByUsername(username)
                .orElseThrow();

        LocalDate today = LocalDate.now();

        DayOfWeek todayWeek = today.getDayOfWeek();

        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        long todaySessions =
                bookingRepository.countTodaySessions(
                        coach.getId(),
                        todayWeek,
                        today
                );

        long weekSessions =
                bookingRepository.countWeekSessions(
                        coach.getId(),
                        startOfWeek,
                        endOfWeek
                );

        double weekRevenue =
                bookingRepository.sumWeekRevenue(
                        coach.getId(),
                        startOfWeek,
                        endOfWeek
                );

        long pendingBookings =
                bookingRepository.countByCoachIdAndStatus(
                        coach.getId(),
                        BookingStatus.PENDING
                );

        return CoachDashboardResponse.builder()
                .todaySessions(todaySessions)
                .weekSessions(weekSessions)
                .weekRevenue(weekRevenue)
                .pendingBookings(pendingBookings)
                .build();
    }
}
