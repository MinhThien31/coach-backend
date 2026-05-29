package com.minhthien.web.coach.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class ProgressApiResponses {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressOverviewResponse {
        private long totalSessions;
        private long trainingHours;
        private int averageAiScore;
        private long activeCoaches;
        private int streakDays;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BodyMetricResponse {
        private Long id;
        private LocalDate measuredAt;
        private Double weight;
        private Double bodyFat;
        private Double muscleMass;
        private String note;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExerciseProgressResponse {
        private Long id;
        private String exerciseName;
        private LocalDate measuredAt;
        private Double value;
        private String unit;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AchievementResponse {
        private Long id;
        private String title;
        private String description;
        private LocalDate achievedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreakResponse {
        private int streakDays;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeatmapPointResponse {
        private LocalDate date;
        private int value;
    }
}
