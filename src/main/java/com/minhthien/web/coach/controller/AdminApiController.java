package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.request.AdminApiRequests;
import com.minhthien.web.coach.dto.response.AdminApiResponses;
import com.minhthien.web.coach.dto.response.ApiResponse;
import com.minhthien.web.coach.enums.SubscriptionPlanCode;
import com.minhthien.web.coach.enums.UserRole;
import com.minhthien.web.coach.service.AdminApiService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AdminApiController {

    private final AdminApiService adminApiService;

    @GetMapping("/users")
    public ApiResponse<Page<AdminApiResponses.AdminUserResponse>> getUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(adminApiService.getUsers(role, status, keyword, page, size));
    }

    @GetMapping("/users/{id}")
    public ApiResponse<AdminApiResponses.AdminUserResponse> getUser(@PathVariable Long id) {
        return ApiResponse.success(adminApiService.getUser(id));
    }

    @PutMapping("/users/{id}/status")
    public ApiResponse<AdminApiResponses.AdminUserResponse> updateUserStatus(
            @PathVariable Long id,
            @RequestParam(required = false) Boolean active,
            @RequestBody(required = false) AdminApiRequests.UserStatusRequest request
    ) {
        return ApiResponse.success(adminApiService.updateUserStatus(id, request, active));
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        adminApiService.softDeleteUser(id);
        return ApiResponse.success("User deactivated successfully", null);
    }

    @GetMapping("/dashboard/overview")
    public ApiResponse<AdminApiResponses.DashboardOverviewResponse> getDashboardOverview() {
        return ApiResponse.success(adminApiService.getDashboardOverview());
    }

    @GetMapping("/dashboard/revenue-chart")
    public ApiResponse<List<AdminApiResponses.RevenueChartPointResponse>> getRevenueChart(
            @RequestParam(defaultValue = "month") String range
    ) {
        return ApiResponse.success(adminApiService.getRevenueChart(range));
    }

    @GetMapping("/dashboard/recent-transactions")
    public ApiResponse<List<AdminApiResponses.AdminTransactionResponse>> getRecentTransactions() {
        return ApiResponse.success(adminApiService.getRecentTransactions());
    }

    @GetMapping("/dashboard/alerts")
    public ApiResponse<List<AdminApiResponses.AdminAlertResponse>> getAlerts() {
        return ApiResponse.success(adminApiService.getAlerts());
    }

    @GetMapping("/transactions")
    public ApiResponse<Page<AdminApiResponses.AdminTransactionResponse>> getTransactions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) SubscriptionPlanCode coachPlan,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(adminApiService.getTransactions(status, coachPlan, keyword, from, to, page, size));
    }

    @GetMapping("/transactions/summary")
    public ApiResponse<AdminApiResponses.TuitionTransactionSummaryResponse> getTransactionSummary(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) SubscriptionPlanCode coachPlan,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(adminApiService.getTransactionSummary(status, coachPlan, keyword, from, to));
    }

    @GetMapping("/transactions/{id}")
    public ApiResponse<AdminApiResponses.AdminTransactionResponse> getTransaction(@PathVariable Long id) {
        return ApiResponse.success(adminApiService.getTransaction(id));
    }

    @GetMapping("/subscriptions")
    public ApiResponse<Page<AdminApiResponses.AdminSubscriptionResponse>> getSubscriptions(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) SubscriptionPlanCode plan,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(adminApiService.getSubscriptions(role, plan, status, keyword, page, size));
    }

    @GetMapping("/subscriptions/stats")
    public ApiResponse<AdminApiResponses.SubscriptionStatsResponse> getSubscriptionStats() {
        return ApiResponse.success(adminApiService.getSubscriptionStats());
    }

    @GetMapping("/subscriptions/summary")
    public ApiResponse<AdminApiResponses.SubscriptionSummaryResponse> getSubscriptionSummary() {
        return ApiResponse.success(adminApiService.getSubscriptionSummary());
    }

    @PutMapping("/subscriptions/{userId}")
    public ApiResponse<AdminApiResponses.AdminSubscriptionResponse> updateSubscription(
            @PathVariable Long userId,
            @RequestBody AdminApiRequests.SubscriptionUpdateRequest request
    ) {
        return ApiResponse.success(adminApiService.updateSubscription(userId, request));
    }

    @GetMapping("/finance/overview")
    public ApiResponse<AdminApiResponses.FinanceOverviewResponse> getFinanceOverview() {
        return ApiResponse.success(adminApiService.getFinanceOverview());
    }

    @GetMapping("/finance/monthly-revenue")
    public ApiResponse<List<AdminApiResponses.RevenueChartPointResponse>> getMonthlyRevenue() {
        return ApiResponse.success(adminApiService.getMonthlyRevenue());
    }

    @GetMapping("/finance/commission-by-plan")
    public ApiResponse<List<AdminApiResponses.CommissionByPlanResponse>> getCommissionByPlan() {
        return ApiResponse.success(adminApiService.getCommissionByPlan());
    }

    @GetMapping("/finance/revenue-by-source")
    public ApiResponse<List<AdminApiResponses.RevenueBySourceResponse>> getRevenueBySource() {
        return ApiResponse.success(adminApiService.getRevenueBySource());
    }

    @GetMapping("/finance/top-coaches")
    public ApiResponse<List<AdminApiResponses.TopCoachResponse>> getTopCoaches() {
        return ApiResponse.success(adminApiService.getTopCoaches());
    }
}
