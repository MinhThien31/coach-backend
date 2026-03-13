package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.request.ChangeSubscriptionPlanRequest;
import com.minhthien.web.coach.dto.response.CurrentSubscriptionResponse;
import com.minhthien.web.coach.dto.response.SubscriptionCatalogResponse;
import com.minhthien.web.coach.dto.response.SubscriptionChangeResponse;
import com.minhthien.web.coach.enums.SubscriptionBillingCycle;

public interface SubscriptionService {

    SubscriptionCatalogResponse getTraineeCatalog(Long currentUserId, SubscriptionBillingCycle billingCycle);

    SubscriptionCatalogResponse getCoachCatalog(Long currentUserId, SubscriptionBillingCycle billingCycle);

    CurrentSubscriptionResponse getCurrentSubscription(Long currentUserId);

    SubscriptionChangeResponse changePlan(Long currentUserId, ChangeSubscriptionPlanRequest request);

    SubscriptionChangeResponse changeCoachPlan(Long currentUserId, ChangeSubscriptionPlanRequest request);

}
