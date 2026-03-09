package com.minhthien.web.coach.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingRequest {

    private Long coachId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
