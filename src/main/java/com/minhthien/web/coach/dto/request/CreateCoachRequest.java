package com.minhthien.web.coach.dto.request;

import com.minhthien.web.coach.enums.CoachTeachingType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateCoachRequest {

    private Long categoryId;

    private Double price;

    private Integer experienceYears;
    private MultipartFile avatar;
    private String location;

    private CoachTeachingType teachingType;

    private String bio;
}