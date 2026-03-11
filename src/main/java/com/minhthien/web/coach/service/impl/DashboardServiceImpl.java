package com.minhthien.web.coach.service.impl;

import com.minhthien.web.coach.dto.response.CoachDashboardResponse;
import com.minhthien.web.coach.dto.response.DashboardStatsResponse;
import com.minhthien.web.coach.entity.Booking;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.enums.BookingStatus;
import com.minhthien.web.coach.repository.BookingRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    public DashboardStatsResponse getStats() {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow();

        List<Booking> bookings = bookingRepository.findByTraineeId(user.getId());

        LocalDate now = LocalDate.now();

        long thisMonth = bookings.stream()
                .filter(b -> b.getStartDate().getMonth() == now.getMonth())
                .count();

        long completed = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .count();

        long upcoming = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING)
                .count();

        double spending = bookings.stream()
                .mapToDouble(Booking::getPrice)
                .sum();

        return DashboardStatsResponse.builder()
                .sessionsThisMonth(thisMonth)
                .completedSessions(completed)
                .upcomingSessions(upcoming)
                .monthlySpending(spending)
                .build();
    }

    @Override
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