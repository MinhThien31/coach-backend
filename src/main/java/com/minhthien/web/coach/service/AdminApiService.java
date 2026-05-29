package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.request.AdminApiRequests;
import com.minhthien.web.coach.dto.response.AdminApiResponses;
import com.minhthien.web.coach.enums.SubscriptionPlanCode;
import com.minhthien.web.coach.enums.UserRole;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface AdminApiService {
    Page<AdminApiResponses.AdminUserResponse> getUsers(UserRole role, String status, String keyword, int page, int size);
    AdminApiResponses.AdminUserResponse getUser(Long id);
    AdminApiResponses.AdminUserResponse updateUserStatus(Long id, AdminApiRequests.UserStatusRequest request, Boolean active);
    void softDeleteUser(Long id);

    AdminApiResponses.DashboardOverviewResponse getDashboardOverview();
    List<AdminApiResponses.RevenueChartPointResponse> getRevenueChart(String range);
    List<AdminApiResponses.AdminTransactionResponse> getRecentTransactions();
    List<AdminApiResponses.AdminAlertResponse> getAlerts();

    Page<AdminApiResponses.AdminTransactionResponse> getTransactions(String status, SubscriptionPlanCode coachPlan, String keyword, LocalDate from, LocalDate to, int page, int size);
    AdminApiResponses.TuitionTransactionSummaryResponse getTransactionSummary(String status, SubscriptionPlanCode coachPlan, String keyword, LocalDate from, LocalDate to);
    AdminApiResponses.AdminTransactionResponse getTransaction(Long id);

    Page<AdminApiResponses.AdminSubscriptionResponse> getSubscriptions(UserRole role, SubscriptionPlanCode plan, String status, String keyword, int page, int size);
    AdminApiResponses.SubscriptionStatsResponse getSubscriptionStats();
    AdminApiResponses.SubscriptionSummaryResponse getSubscriptionSummary();
    AdminApiResponses.AdminSubscriptionResponse updateSubscription(Long userId, AdminApiRequests.SubscriptionUpdateRequest request);

    AdminApiResponses.FinanceOverviewResponse getFinanceOverview();
    List<AdminApiResponses.RevenueChartPointResponse> getMonthlyRevenue();
    List<AdminApiResponses.CommissionByPlanResponse> getCommissionByPlan();
    List<AdminApiResponses.RevenueBySourceResponse> getRevenueBySource();
    List<AdminApiResponses.TopCoachResponse> getTopCoaches();
}
