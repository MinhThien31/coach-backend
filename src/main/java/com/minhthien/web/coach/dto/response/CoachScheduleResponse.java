package com.minhthien.web.coach.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CoachScheduleResponse {

    private String dayOfWeek;

    private String startTime;

    private String endTime;
}
