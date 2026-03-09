package com.minhthien.web.coach.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateCoachRequest {

    private Long categoryId;

    private Double price;

    private Integer experienceYears;
    private MultipartFile avatar;
    private String location;

    private String bio;
}