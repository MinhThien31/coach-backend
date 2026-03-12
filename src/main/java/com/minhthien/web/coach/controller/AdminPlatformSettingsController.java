package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.request.UpdateCommissionSettingsRequest;
import com.minhthien.web.coach.dto.request.UpdateSubscriptionPricesRequest;
import com.minhthien.web.coach.dto.response.AdminCommissionSettingsResponse;
import com.minhthien.web.coach.dto.response.AdminPlatformSettingsResponse;
import com.minhthien.web.coach.dto.response.AdminSubscriptionPricesResponse;
import com.minhthien.web.coach.dto.response.ApiResponse;
import com.minhthien.web.coach.service.AdminPlatformSettingsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/platform-settings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminPlatformSettingsController {

    private final AdminPlatformSettingsService adminPlatformSettingsService;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminPlatformSettingsResponse>> getSettings() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Admin platform settings loaded successfully",
                        adminPlatformSettingsService.getSettings()
                )
        );
    }

    @PutMapping("/commission-rates")
    public ResponseEntity<ApiResponse<AdminCommissionSettingsResponse>> updateCommissionRates(
            @Valid @RequestBody UpdateCommissionSettingsRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Commission rates updated successfully",
                        adminPlatformSettingsService.updateCommissionSettings(request)
                )
        );
    }

    @PutMapping("/subscription-prices")
    public ResponseEntity<ApiResponse<AdminSubscriptionPricesResponse>> updateSubscriptionPrices(
            @Valid @RequestBody UpdateSubscriptionPricesRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Subscription prices updated successfully",
                        adminPlatformSettingsService.updateSubscriptionPrices(request)
                )
        );
    }
}
