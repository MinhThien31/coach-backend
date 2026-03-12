package com.minhthien.web.coach.dto.response;

import com.minhthien.web.coach.enums.SubscriptionPlanCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanCardResponse {
    private SubscriptionPlanCode planCode;
    private String displayName;
    private String description;
    private Long monthlyPrice;
    private Long billingPrice;
    private String displayPrice;
    private String billingLabel;
    private Boolean highlighted;
    private String ribbonText;
    private Boolean current;
    private Boolean exactCurrent;
    private String actionLabel;
    private String actionType;
    private List<SubscriptionFeatureResponse> features;
}
