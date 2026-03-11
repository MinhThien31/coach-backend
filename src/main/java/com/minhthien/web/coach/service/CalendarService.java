package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.response.*;

import java.time.LocalDate;
import java.util.List;

public interface CalendarService {

    List<CalendarMonthResponse> getMonth(int year, int month);

    List<CalendarWeekResponse> getWeek(LocalDate startDate);

    List<BookingListResponse> bookingList(String status);

    List<CoachWeekResponse> getCoachWeek(LocalDate startDate);

    List<CoachMonthResponse> getCoachMonth(int year, int month);

    List<BookingListResponse> getCoachList();


}