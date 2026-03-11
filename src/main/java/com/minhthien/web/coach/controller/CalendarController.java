package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.response.BookingListResponse;
import com.minhthien.web.coach.dto.response.CalendarMonthResponse;
import com.minhthien.web.coach.dto.response.CalendarWeekResponse;
import com.minhthien.web.coach.service.CalendarService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendar/traines")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/month")
    public List<CalendarMonthResponse> getMonth(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return calendarService.getMonth(year, month);
    }
    @GetMapping("/week")
    public List<CalendarWeekResponse> getWeek(
            @RequestParam LocalDate startDate
    ) {
        return calendarService.getWeek(startDate);
    }

    @GetMapping("/list")
    public List<BookingListResponse> bookingList(
            @RequestParam(defaultValue = "ALL") String status
    ) {
        return calendarService.bookingList(status);
    }

}