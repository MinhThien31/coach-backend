package com.minhthien.web.coach.dto.response;

import com.minhthien.web.coach.enums.CoachTeachingType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CoachResponse {

    private Long id;

    private String fullName;

    private String avatar;

    private String category;

    private Double price;

    private Double rating;

    private Integer reviewCount;

    private String bio;

    private CoachTeachingType teachingType;

    private String location;
}
