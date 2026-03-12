package com.minhthien.web.coach.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SubmissionItemResponse {

    private Long submissionId;

    private String traineeName;

    private String avatar;

    private LocalDateTime submittedAt;

    private Double score;

    private String status;
}
