package com.minhthien.web.coach.dto.response;

import com.minhthien.web.coach.enums.BookingStatus;
import com.minhthien.web.coach.enums.BookingType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class BookingListResponse {

    private Long id;

    private String coachName;

    private String coachAvatar;

    private String traineeName;

    private String traineeAvatar;

    private String sport;

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    private BookingType type;

    private Double price;

    private BookingStatus status;

    private String cancellationReason;

    private String cancelledBy;

    private LocalDateTime cancelledAt;
}
