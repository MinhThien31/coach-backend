package com.minhthien.web.coach.dto.request;

import lombok.Data;

@Data
public class CreateScheduleRequest {

    private Long coachId;

    private String dayOfWeek;

    private String startTime;

    private String endTime;

}
