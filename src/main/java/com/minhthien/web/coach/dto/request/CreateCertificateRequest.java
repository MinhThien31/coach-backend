package com.minhthien.web.coach.dto.request;

import lombok.Data;

@Data
public class CreateCertificateRequest {

    private Long coachId;

    private String name;

}
