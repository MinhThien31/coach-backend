package com.minhthien.web.coach.dto.response;

import com.minhthien.web.coach.enums.SubmissionStatus;
import com.minhthien.web.coach.enums.VideoType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class VideoApiResponses {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoResponse {
        private Long id;
        private String title;
        private String description;
        private String videoUrl;
        private String thumbnailUrl;
        private String category;
        private Long categoryId;
        private String coachName;
        private Long coachUserId;
        private VideoType videoType;
        private String format;
        private String resolution;
        private Double size;
        private Long duration;
        private String difficulty;
        private String visibility;
        private Long viewCount;
        private Long likes;
        private Boolean isPremium;
        private Boolean liked;
        private Boolean saved;
        private List<String> tags;
        private LocalDate uploadDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoAnalyticsResponse {
        private Long videoId;
        private Long views;
        private Long likes;
        private Long saves;
        private Long submissions;
        private Long pendingSubmissions;
        private Double averageScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmissionResponse {
        private Long id;
        private Long videoId;
        private String videoTitle;
        private Long traineeId;
        private String traineeName;
        private String videoUrl;
        private String note;
        private SubmissionStatus status;
        private Double totalScore;
        private String feedback;
        private LocalDateTime submittedAt;
    }
}
