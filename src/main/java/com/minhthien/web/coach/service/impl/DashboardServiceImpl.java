package com.minhthien.web.coach.service.impl;

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
}