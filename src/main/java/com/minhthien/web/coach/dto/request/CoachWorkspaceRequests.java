package com.minhthien.web.coach.dto.request;

import lombok.Data;

import java.time.LocalDate;

public class CoachWorkspaceRequests {

    @Data
    public static class StudentTaskRequest {
        private String title;
        private String description;
        private String status;
        private LocalDate dueDate;
        private Boolean completed;
    }

    @Data
    public static class StudentNoteRequest {
        private String title;
        private String content;
    }
}
