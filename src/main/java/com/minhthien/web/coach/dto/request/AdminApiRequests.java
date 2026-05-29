package com.minhthien.web.coach.dto.request;

import com.minhthien.web.coach.enums.SubscriptionBillingCycle;
import com.minhthien.web.coach.enums.SubscriptionPlanCode;
import lombok.Data;

import java.time.LocalDateTime;

public class AdminApiRequests {

    @Data
    public static class UserStatusRequest {
        private Boolean active;
    }

    @Data
    public static class SubscriptionUpdateRequest {
        private SubscriptionPlanCode planCode;
        private SubscriptionBillingCycle billingCycle;
        private Boolean active;
        private LocalDateTime expiresAt;
    }
}
