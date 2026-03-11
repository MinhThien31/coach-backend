package com.minhthien.web.coach.dto.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TraineeResponse {

    private Long id;

    private String fullName;

    private String avatar;

    private String goal;

    private Integer age;

    private Double weight;

    private Double height;

    private String phone;

}

