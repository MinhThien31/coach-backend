package com.minhthien.web.coach.dto.request;

import com.minhthien.web.coach.enums.SubscriptionBillingCycle;
import com.minhthien.web.coach.enums.SubscriptionPlanCode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeSubscriptionPlanRequest {

    @NotNull(message = "Plan code is required")
    private SubscriptionPlanCode planCode;

    @NotNull(message = "Billing cycle is required")
    private SubscriptionBillingCycle billingCycle;
}
