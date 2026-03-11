package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.response.*;
import com.minhthien.web.coach.service.CalendarService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/coach/calendar")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CoachCalendarController {

    private final CalendarService calendarService;

    @GetMapping("/week")
    public List<CoachWeekResponse> getWeek(
            @RequestParam LocalDate startDate
    ) {
        return calendarService.getCoachWeek(startDate);
    }

    @GetMapping("/month")
    public List<CoachMonthResponse> getMonth(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return calendarService.getCoachMonth(year, month);
    }

    @GetMapping("/list")
    public List<BookingListResponse> getList() {
        return calendarService.getCoachList();
    }

}
