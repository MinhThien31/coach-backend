package com.minhthien.web.coach.dto.request;

import lombok.Data;

@Data
public class CreateSpecializationRequest {

    private Long coachId;

    private String name;

}
