package com.minhthien.web.coach.dto.response;

import com.minhthien.web.coach.enums.BookingType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class BookingListResponse {

    private Long id;

    private String coachName;

    private String sport;

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    private BookingType type;

    private Double price;

    private String status;
}