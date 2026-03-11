package com.minhthien.web.coach.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CalendarSessionItem {

    private Long bookingId;

    private String sport;

    private String coachName;

}