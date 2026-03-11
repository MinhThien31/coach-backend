package com.minhthien.web.coach.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class CalendarMonthResponse {

    private LocalDate date;

    private List<CalendarSessionItem> sessions;

}