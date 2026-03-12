package com.minhthien.web.coach.dto.response;

import com.minhthien.web.coach.enums.SubscriptionBillingCycle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCatalogResponse {
    private String audience;
    private String pageTitle;
    private String pageSubtitle;
    private SubscriptionBillingCycle selectedBillingCycle;
    private Integer yearlyDiscountMonths;
    private CurrentSubscriptionResponse currentSubscription;
    private List<SubscriptionPlanCardResponse> plans;
}
