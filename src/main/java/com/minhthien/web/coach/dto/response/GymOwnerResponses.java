package com.minhthien.web.coach.dto.response;

import com.minhthien.web.coach.enums.BookingType;
import com.minhthien.web.coach.enums.GymCoachStatus;
import com.minhthien.web.coach.enums.GymProfileStatus;
import com.minhthien.web.coach.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class GymOwnerResponses {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GymProfileResponse {
        private Long id;
        private Long ownerId;
        private String ownerName;
        private String ownerEmail;
        private String name;
        private String address;
        private String hotline;
        private String description;
        private String logoUrl;
        private String coverUrl;
        private GymProfileStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GymCoachResponse {
        private Long id;
        private Long coachProfileId;
        private Long coachUserId;
        private String coachName;
        private String coachEmail;
        private String avatarUrl;
        private String categoryName;
        private Double price;
        private Double rating;
        private GymCoachStatus status;
        private LocalDateTime joinedAt;
        private LocalDateTime removedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GymBookingResponse {
        private Long id;
        private Long coachProfileId;
        private String coachName;
        private String traineeName;
        private LocalDate startDate;
        private LocalDate endDate;
        private DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private Double price;
        private String status;
        private BookingType type;
        private Boolean paymentSettled;
        private Long settledAmount;
        private Long adminCommissionAmount;
        private Long coachPayoutAmount;
        private Long payoutRecipientUserId;
        private UserRole payoutRecipientRole;
        private String payoutRecipientName;
        private LocalDateTime createdAt;
        private LocalDateTime settledAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GymOverviewResponse {
        private GymProfileResponse profile;
        private Long activeCoachCount;
        private Long totalBookingCount;
        private Long monthBookingCount;
        private Long settledRevenue;
        private Long monthSettledRevenue;
        private Long platformCommission;
        private WalletResponse wallet;
        private List<GymCoachResponse> coaches;
        private List<GymBookingResponse> recentBookings;
    }
}
