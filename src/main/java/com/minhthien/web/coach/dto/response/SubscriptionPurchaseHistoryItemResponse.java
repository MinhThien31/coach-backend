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
public class SubscriptionPurchaseHistoryItemResponse {
    private Long transactionId;
    private SubscriptionPlanCode planCode;
    private String displayName;
    private SubscriptionBillingCycle billingCycle;
    private Long amount;
    private String displayAmount;
    private String description;
    private String referenceId;
    private LocalDateTime purchasedAt;
}
