package com.minhthien.web.coach.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.minhthien.web.coach.enums.BookingType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
public class BookingResponse {

    private Long id;

    private String coachName;

    private String traineeName;

    private LocalDate startDate;

    private LocalDate endDate;

    private DayOfWeek dayOfWeek;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    private Double price;

    private BookingType type;

    private String note;

    private String status;
}
