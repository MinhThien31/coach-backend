package com.minhthien.web.coach.dto.request;

import lombok.Data;

import java.time.LocalDate;

public class ProgressApiRequests {

    @Data
    public static class BodyMetricRequest {
        private LocalDate measuredAt;
        private Double weight;
        private Double bodyFat;
        private Double muscleMass;
        private String note;
    }

    @Data
    public static class ExerciseProgressRequest {
        private String exerciseName;
        private LocalDate measuredAt;
        private Double value;
        private String unit;
    }
}
