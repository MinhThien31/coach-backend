package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.request.UpdateCommissionSettingsRequest;
import com.minhthien.web.coach.dto.request.UpdateSubscriptionPricesRequest;
import com.minhthien.web.coach.dto.response.AdminCommissionSettingsResponse;
import com.minhthien.web.coach.dto.response.AdminPlatformSettingsResponse;
import com.minhthien.web.coach.dto.response.AdminSubscriptionPricesResponse;

public interface AdminPlatformSettingsService {

    AdminPlatformSettingsResponse getSettings();

    AdminCommissionSettingsResponse updateCommissionSettings(UpdateCommissionSettingsRequest request);

    AdminSubscriptionPricesResponse updateSubscriptionPrices(UpdateSubscriptionPricesRequest request);
}
