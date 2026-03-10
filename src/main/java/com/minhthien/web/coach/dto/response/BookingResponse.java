package com.minhthien.web.coach.dto.response;

import com.minhthien.web.coach.enums.BookingType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class BookingResponse {

    private Long id;

    private String coachName;

    private String traineeName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Double price;

    private BookingType type;

    private String note;

    private String status;
}
