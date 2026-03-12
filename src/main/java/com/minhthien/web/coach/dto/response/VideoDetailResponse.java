package com.minhthien.web.coach.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class VideoDetailResponse {

    private Long videoId;

    private String title;

    private String category;

    private String format;

    private Double size;

    private String resolution;

    private LocalDate uploadDate;

    private List<String> tags;

    private List<SubmissionItemResponse> submissions;
}
