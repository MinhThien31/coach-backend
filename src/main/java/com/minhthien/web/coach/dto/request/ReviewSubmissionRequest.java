package com.minhthien.web.coach.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ReviewSubmissionRequest {

    private Integer postureScore;

    private Integer techniqueScore;

    private Integer rhythmScore;

    private Integer strengthScore;

    private String feedback;

    private List<CommentRequest> comments;
}
