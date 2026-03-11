package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.response.BookingListResponse;
import com.minhthien.web.coach.dto.response.CalendarMonthResponse;
import com.minhthien.web.coach.dto.response.CalendarWeekResponse;

import java.time.LocalDate;
import java.util.List;

public interface CalendarService {

    List<CalendarMonthResponse> getMonth(int year, int month);

    List<CalendarWeekResponse> getWeek(LocalDate startDate);

    List<BookingListResponse> bookingList(String status);


}