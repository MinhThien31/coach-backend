package com.minhthien.web.coach.dto.response;

import com.minhthien.web.coach.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class CoachWorkspaceResponses {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IncomeOverviewResponse {
        private long monthRevenue;
        private long weekRevenue;
        private long totalRevenue;
        private long availableBalance;
        private long pendingWithdrawals;
        private long platformCommission;
        private long completedBookings;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartPointResponse {
        private String period;
        private long value;
        private long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopStudentResponse {
        private Long traineeId;
        private String traineeName;
        private long sessions;
        private long revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyticsOverviewResponse {
        private long totalBookings;
        private long pendingBookings;
        private long confirmedBookings;
        private long completedBookings;
        private long totalStudents;
        private long totalRevenue;
        private long totalVideos;
        private long totalVideoViews;
        private double averageRating;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentSummaryResponse {
        private Long traineeId;
        private Long userId;
        private String fullName;
        private String avatar;
        private String goal;
        private String phone;
        private long sessions;
        private long completedSessions;
        private LocalDate lastSessionDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentDetailResponse {
        private StudentSummaryResponse profile;
        private List<SessionResponse> recentSessions;
        private List<TaskResponse> tasks;
        private List<NoteResponse> notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionResponse {
        private Long bookingId;
        private LocalDate startDate;
        private LocalDate endDate;
        private String dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private BookingStatus status;
        private Double price;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentProgressResponse {
        private Long traineeId;
        private long totalSessions;
        private long completedSessions;
        private long pendingSubmissions;
        private long reviewedSubmissions;
        private Double averageSubmissionScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskResponse {
        private Long id;
        private String title;
        private String description;
        private String status;
        private LocalDate dueDate;
        private Boolean completed;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoteResponse {
        private Long id;
        private String title;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
