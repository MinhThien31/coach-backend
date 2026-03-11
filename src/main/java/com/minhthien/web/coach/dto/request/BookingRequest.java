package com.minhthien.web.coach.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.minhthien.web.coach.enums.BookingType;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
public class BookingRequest {

    private Long coachId;

    private LocalDate startDate;

    private LocalDate endDate;

    private DayOfWeek dayOfWeek;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    private BookingType type;

    private String note;
}
