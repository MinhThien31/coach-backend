package com.minhthien.web.coach.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ReviewResponse {

    private String userName;

    private String avatar;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;
}