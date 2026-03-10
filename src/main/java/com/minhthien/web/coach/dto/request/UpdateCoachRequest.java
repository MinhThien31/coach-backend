package com.minhthien.web.coach.dto.request;

import com.minhthien.web.coach.enums.CoachTeachingType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateCoachRequest {

    private Long categoryId;

    private Double price;

    private Integer experienceYears;

    private String bio;

    private CoachTeachingType teachingType;
    private String location;
    private MultipartFile avatar;
}
