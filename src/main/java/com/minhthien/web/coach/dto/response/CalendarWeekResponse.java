package com.minhthien.web.coach.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class CalendarWeekResponse {

    private Long bookingId;

    private String coachName;

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

}