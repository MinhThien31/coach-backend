package com.minhthien.web.coach.dto.request;

import com.minhthien.web.coach.enums.VideoType;
import lombok.Data;

public class VideoApiRequests {

    @Data
    public static class UpdateCoachVideoRequest {
        private String title;
        private String description;
        private String thumbnailUrl;
        private String format;
        private String resolution;
        private Long duration;
        private String difficulty;
        private String visibility;
        private Boolean isPremium;
        private Long categoryId;
        private VideoType videoType;
        private String tags;
    }
}
