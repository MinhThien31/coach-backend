package com.minhthien.web.coach.service.impl;

import com.minhthien.web.coach.dto.response.BookingListResponse;
import com.minhthien.web.coach.dto.response.CalendarMonthResponse;
import com.minhthien.web.coach.dto.response.CalendarSessionItem;
import com.minhthien.web.coach.dto.response.CalendarWeekResponse;
import com.minhthien.web.coach.entity.Booking;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.enums.BookingStatus;
import com.minhthien.web.coach.repository.BookingRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    public List<CalendarMonthResponse> getMonth(int year, int month) {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow();

        List<Booking> bookings =
                bookingRepository.findByTraineeId(user.getId());

        Map<LocalDate, List<CalendarSessionItem>> calendar =
                new HashMap<>();

        for (Booking booking : bookings) {

            LocalDate date = booking.getStartDate();

            while (!date.isAfter(booking.getEndDate())) {

                if (date.getDayOfWeek() == booking.getDayOfWeek()) {

                    if (date.getYear() == year &&
                            date.getMonthValue() == month) {

                        calendar
                                .computeIfAbsent(date, d -> new ArrayList<>())
                                .add(
                                        CalendarSessionItem.builder()
                                                .bookingId(booking.getId())
                                                .sport("Training")
                                                .coachName(
                                                        booking.getCoach()
                                                                .getUser()
                                                                .getFullName()
                                                )
                                                .build()
                                );
                    }
                }

                date = date.plusDays(1);
            }
        }

        List<CalendarMonthResponse> result = new ArrayList<>();

        for (Map.Entry<LocalDate, List<CalendarSessionItem>> entry : calendar.entrySet()) {

            result.add(
                    CalendarMonthResponse.builder()
                            .date(entry.getKey())
                            .sessions(entry.getValue())
                            .build()
            );
        }

        return result;
    }

    @Override
    public List<CalendarWeekResponse> getWeek(LocalDate startDate) {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow();

        List<Booking> bookings =
                bookingRepository.findByTraineeId(user.getId());

        LocalDate endDate = startDate.plusDays(6);

        List<CalendarWeekResponse> result = new ArrayList<>();

        for (Booking booking : bookings) {

            LocalDate date = booking.getStartDate();

            while (!date.isAfter(booking.getEndDate())) {

                if (date.getDayOfWeek() == booking.getDayOfWeek()) {

                    if (!date.isBefore(startDate) && !date.isAfter(endDate)) {

                        result.add(
                                CalendarWeekResponse.builder()
                                        .bookingId(booking.getId())
                                        .coachName(
                                                booking.getCoach()
                                                        .getUser()
                                                        .getFullName()
                                        )
                                        .date(date)
                                        .startTime(booking.getStartTime())
                                        .endTime(booking.getEndTime())
                                        .build()
                        );

                    }
                }

                date = date.plusDays(1);
            }
        }

        return result;
    }

    @Override
    public List<BookingListResponse> bookingList(String status) {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User trainee = userRepository
                .findByUsername(username)
                .orElseThrow();

        List<Booking> bookings =
                bookingRepository.findByTraineeId(trainee.getId());

        LocalDate today = LocalDate.now();

        return bookings.stream()

                .filter(b -> {

                    if (status.equalsIgnoreCase("ALL"))
                        return true;

                    if (status.equalsIgnoreCase("UPCOMING"))
                        return b.getStartDate().isAfter(today);

                    if (status.equalsIgnoreCase("COMPLETED"))
                        return b.getStatus() == BookingStatus.COMPLETED;

                    if (status.equalsIgnoreCase("PENDING"))
                        return b.getStatus() == BookingStatus.PENDING;

                    if (status.equalsIgnoreCase("CANCELLED"))
                        return b.getStatus() == BookingStatus.CANCELLED;

                    return true;
                })

                .map(b -> BookingListResponse.builder()
                        .id(b.getId())
                        .coachName(b.getCoach().getUser().getFullName())
                        .sport("Training")
                        .date(b.getStartDate())
                        .startTime(b.getStartTime())
                        .endTime(b.getEndTime())
                        .type(b.getType())
                        .price(b.getPrice())
                        .status(b.getStatus().name())
                        .build())

                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))

                .toList();
    }


}