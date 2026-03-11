package com.minhthien.web.coach.dto.response;

import com.minhthien.web.coach.enums.BookingStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoachWeekResponse {

    private Long bookingId;

    private String traineeName;

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    private BookingStatus status;

    private String location;

}
