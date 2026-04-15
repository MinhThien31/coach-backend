package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.request.ChangeSubscriptionPlanRequest;
import com.minhthien.web.coach.dto.response.ApiResponse;
import com.minhthien.web.coach.dto.response.CurrentSubscriptionResponse;
import com.minhthien.web.coach.dto.response.SubscriptionCatalogResponse;
import com.minhthien.web.coach.dto.response.SubscriptionChangeResponse;
import com.minhthien.web.coach.dto.response.SubscriptionPurchaseHistoryResponse;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.enums.SubscriptionBillingCycle;
import com.minhthien.web.coach.service.SubscriptionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/packages")
    public ResponseEntity<ApiResponse<SubscriptionCatalogResponse>> getMyPackages(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) SubscriptionBillingCycle billingCycle
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Subscription packages loaded successfully",
                        subscriptionService.getMyPackages(currentUser.getId(), billingCycle)
                )
        );
    }

    @GetMapping("/trainee/catalog")
    public ResponseEntity<ApiResponse<SubscriptionCatalogResponse>> getTraineeCatalog(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) SubscriptionBillingCycle billingCycle
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Trainee subscription catalog loaded successfully",
                        subscriptionService.getTraineeCatalog(currentUser.getId(), billingCycle)
                )
        );
    }

    @GetMapping("/coach/catalog")
    public ResponseEntity<ApiResponse<SubscriptionCatalogResponse>> getCoachCatalog(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) SubscriptionBillingCycle billingCycle
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Coach subscription catalog loaded successfully",
                        subscriptionService.getCoachCatalog(currentUser.getId(), billingCycle)
                )
        );
    }

    @PutMapping("/coach/plan")
    public ResponseEntity<ApiResponse<SubscriptionChangeResponse>> changeCoachPlan(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangeSubscriptionPlanRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Coach subscription updated successfully",
                        subscriptionService.changeCoachPlan(currentUser.getId(), request)
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CurrentSubscriptionResponse>> getCurrentSubscription(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Current subscription loaded successfully",
                        subscriptionService.getCurrentSubscription(currentUser.getId())
                )
        );
    }

    @GetMapping("/purchase-history")
    public ResponseEntity<ApiResponse<SubscriptionPurchaseHistoryResponse>> getPurchaseHistory(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Subscription purchase history loaded successfully",
                        subscriptionService.getPurchaseHistory(currentUser.getId())
                )
        );
    }

    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse<SubscriptionChangeResponse>> purchasePackage(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangeSubscriptionPlanRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Subscription purchased successfully",
                        subscriptionService.purchasePackage(currentUser.getId(), request)
                )
        );
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<SubscriptionChangeResponse>> changePlan(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangeSubscriptionPlanRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Subscription updated successfully",
                        subscriptionService.changePlan(currentUser.getId(), request)
                )
        );
    }
}
