package com.minhthien.web.coach.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class CoachScheduleResponse {

    private Long id;

    private LocalDate startDate;

    private LocalDate endDate;

    private String dayOfWeek;

    private String startTime;

    private String endTime;

    private Boolean available;

    private String status;

    private Long bookingId;

    private String bookingStatus;
}
