package com.minhthien.web.coach.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateTraineeRequest {

    private String goal;

    private Integer age;

    private Double weight;

    private Double height;

    private String phone;

    private MultipartFile avatar;
}
