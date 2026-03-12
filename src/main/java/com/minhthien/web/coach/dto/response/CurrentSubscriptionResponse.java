package com.minhthien.web.coach.dto.response;

import com.minhthien.web.coach.enums.SubscriptionBillingCycle;
import com.minhthien.web.coach.enums.SubscriptionPlanCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentSubscriptionResponse {
    private SubscriptionPlanCode planCode;
    private String displayName;
    private String statusLabel;
    private String note;
    private Long monthlyPrice;
    private Long billingPrice;
    private String displayPrice;
    private String billingLabel;
    private SubscriptionBillingCycle billingCycle;
    private Integer streakDays;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
}
