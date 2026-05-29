package com.minhthien.web.coach.dto.response;

import com.minhthien.web.coach.enums.SubscriptionBillingCycle;
import com.minhthien.web.coach.enums.SubscriptionPlanCode;
import com.minhthien.web.coach.enums.UserRole;
import com.minhthien.web.coach.enums.WalletTransactionType;
import com.minhthien.web.coach.enums.WalletWithdrawalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class AdminApiResponses {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminUserResponse {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String phone;
        private UserRole role;
        private Boolean active;
        private String avatarUrl;
        private LocalDateTime createdAt;
        private String subscriptionPlanName;
        private Long totalSessions;
        private Long totalSpent;
        private Long totalStudents;
        private Long totalRevenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardOverviewResponse {
        private long totalUsers;
        private long totalCoaches;
        private long totalTrainees;
        private long totalBookings;
        private long pendingBookings;
        private long completedBookings;
        private long totalTransactions;
        private long todayTransactions;
        private long todayRevenue;
        private long monthRevenue;
        private long platformCommission;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueChartPointResponse {
        private String period;
        private long revenue;
        private long bookingRevenue;
        private long subscriptionRevenue;
        private long commission;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminAlertResponse {
        private String type;
        private String severity;
        private String message;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminTransactionResponse {
        private Long id;
        private WalletTransactionType type;
        private Long userId;
        private String userName;
        private String learnerName;
        private String coachName;
        private Long bookingId;
        private String bookingType;
        private SubscriptionPlanCode coachPlanCode;
        private String coachPlanName;
        private Integer commissionRate;
        private Long amount;
        private Long commission;
        private Long coachPayout;
        private String status;
        private String description;
        private String referenceType;
        private String referenceId;
        private SubscriptionPlanCode subscriptionPlanCode;
        private SubscriptionBillingCycle subscriptionBillingCycle;
        private WalletWithdrawalStatus withdrawalStatus;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TuitionTransactionSummaryResponse {
        private long totalAmount;
        private long totalCommission;
        private long totalCoachPayout;
        private long transactionCount;
        private double averageCommissionRate;
        private List<TuitionCommissionByPlanResponse> breakdownByPlan;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TuitionCommissionByPlanResponse {
        private SubscriptionPlanCode planCode;
        private String planName;
        private int commissionRate;
        private long transactionCount;
        private long totalTuition;
        private long commission;
        private long coachPayout;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminSubscriptionResponse {
        private Long id;
        private Long userId;
        private String username;
        private String fullName;
        private String email;
        private UserRole role;
        private SubscriptionPlanCode planCode;
        private String planName;
        private SubscriptionBillingCycle billingCycle;
        private Boolean active;
        private String status;
        private Long monthlyPrice;
        private Long billingPrice;
        private LocalDateTime startedAt;
        private LocalDateTime expiresAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionStatsResponse {
        private long totalSubscriptions;
        private long activeSubscriptions;
        private long freePlans;
        private long proPlans;
        private long premiumPlans;
        private long monthlyPlans;
        private long yearlyPlans;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionSummaryResponse {
        private List<SubscriptionPlanSummaryResponse> learnerPlans;
        private List<SubscriptionPlanSummaryResponse> coachPlans;
        private List<SubscriptionRevenueSummaryResponse> revenueRows;
        private List<SubscriptionRenewalAlertResponse> renewalAlerts;
        private long totalMonthlyRevenue;
        private long activeSubscriptions;
        private long expiredSubscriptions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionPlanSummaryResponse {
        private UserRole role;
        private SubscriptionPlanCode planCode;
        private String planName;
        private long count;
        private double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionRevenueSummaryResponse {
        private UserRole role;
        private SubscriptionPlanCode planCode;
        private String planName;
        private long count;
        private long monthlyPrice;
        private long revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionRenewalAlertResponse {
        private UserRole role;
        private SubscriptionPlanCode planCode;
        private String planName;
        private long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinanceOverviewResponse {
        private long totalRevenue;
        private long monthRevenue;
        private long weekRevenue;
        private long subscriptionRevenue;
        private long bookingRevenue;
        private long platformCommission;
        private long coachPayout;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommissionByPlanResponse {
        private SubscriptionPlanCode planCode;
        private long commission;
        private long transactionCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueBySourceResponse {
        private String source;
        private long revenue;
        private long transactionCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCoachResponse {
        private Long coachId;
        private String coachName;
        private long completedBookings;
        private long revenue;
        private long payout;
    }
}
