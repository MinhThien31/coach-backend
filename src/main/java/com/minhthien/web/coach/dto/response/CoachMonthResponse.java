package com.minhthien.web.coach.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoachMonthResponse {

    private LocalDate date;

    private List<CoachWeekResponse> sessions;

}
