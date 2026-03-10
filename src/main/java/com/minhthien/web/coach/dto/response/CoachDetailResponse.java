package com.minhthien.web.coach.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CoachDetailResponse {

    private Long id;

    private String fullName;

    private String avatar;

    private String category;

    private String location;

    private Double price;

    private Double rating;

    private Long students;

    private Long totalSessions;

    private Integer responseRate;

    private String bio;

    private String teachingType;

    private List<String> specializations;

    private List<String> certificates;

    private List<CoachScheduleResponse> schedules;

    private List<ReviewResponse> reviews;
}
