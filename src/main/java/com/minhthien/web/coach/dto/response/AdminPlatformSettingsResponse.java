package com.minhthien.web.coach.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPlatformSettingsResponse {
    private AdminCommissionSettingsResponse commissionRates;
    private AdminSubscriptionPricesResponse subscriptionPrices;
    private AdminPlatformInfoResponse platformInfo;
}
