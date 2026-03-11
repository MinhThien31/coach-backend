package com.minhthien.web.coach.service.impl;

import com.minhthien.web.coach.dto.response.*;
import com.minhthien.web.coach.entity.Booking;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.enums.BookingStatus;
import com.minhthien.web.coach.repository.BookingRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.service.CalendarService;
import com.minhthien.web.coach.service.UserService;
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
    private final UserService userService;

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
                        .status(b.getStatus())
                        .build())

                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))

                .toList();
    }

    @Override
    public List<CoachWeekResponse> getCoachWeek(LocalDate startDate) {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User coach = userRepository
                .findByUsername(username)
                .orElseThrow();

        LocalDate endDate = startDate.plusDays(6);

        List<Booking> bookings =
                bookingRepository.findByCoachIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        coach.getId(),
                        endDate,
                        startDate
                );

        List<CoachWeekResponse> result = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

            for (Booking booking : bookings) {

                if (date.getDayOfWeek() == booking.getDayOfWeek()) {

                    result.add(
                            CoachWeekResponse.builder()
                                    .bookingId(booking.getId())
                                    .traineeName(booking.getTrainee().getFullName())
                                    .date(date)
                                    .startTime(booking.getStartTime())
                                    .endTime(booking.getEndTime())
                                    .status(booking.getStatus())
                                    .location(booking.getLocation())
                                    .build()
                    );
                }
            }
        }

        return result;
    }

    @Override
    public List<CoachMonthResponse> getCoachMonth(int year, int month) {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User coach = userRepository
                .findByUsername(username)
                .orElseThrow();

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        List<Booking> bookings = bookingRepository.findByCoachId(coach.getId());

        List<CoachMonthResponse> result = new ArrayList<>();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {

            List<CoachWeekResponse> sessions = new ArrayList<>();

            for (Booking booking : bookings) {

                if (date.getDayOfWeek() == booking.getDayOfWeek()) {

                    sessions.add(
                            CoachWeekResponse.builder()
                                    .bookingId(booking.getId())
                                    .traineeName(booking.getTrainee().getFullName())
                                    .date(date)
                                    .startTime(booking.getStartTime())
                                    .endTime(booking.getEndTime())
                                    .status(booking.getStatus())
                                    .location(booking.getLocation())
                                    .build()
                    );
                }
            }

            result.add(
                    CoachMonthResponse.builder()
                            .date(date)
                            .sessions(sessions)
                            .build()
            );
        }

        return result;
    }

    @Override
    public List<BookingListResponse> getCoachList() {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User coach = userRepository
                .findByUsername(username)
                .orElseThrow();

        List<Booking> bookings = bookingRepository.findByCoachId(coach.getId());

        List<BookingListResponse> result = new ArrayList<>();

        for (Booking booking : bookings) {

            result.add(
                    BookingListResponse.builder()
                            .id(booking.getId())
                            .traineeName(booking.getTrainee().getFullName())
                            .date(booking.getStartDate())
                            .startTime(booking.getStartTime())
                            .endTime(booking.getEndTime())
                            .status(booking.getStatus())
                            .price(booking.getPrice())
                            .build()
            );
        }

        return result;
    }


}