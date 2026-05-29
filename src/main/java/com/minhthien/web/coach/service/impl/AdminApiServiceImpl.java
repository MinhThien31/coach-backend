package com.minhthien.web.coach.service.impl;

import com.minhthien.web.coach.dto.request.AdminApiRequests;
import com.minhthien.web.coach.dto.response.AdminApiResponses;
import com.minhthien.web.coach.entity.Booking;
import com.minhthien.web.coach.entity.PlatformSettings;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.entity.UserSubscription;
import com.minhthien.web.coach.entity.WalletTransaction;
import com.minhthien.web.coach.enums.BookingStatus;
import com.minhthien.web.coach.enums.SubscriptionBillingCycle;
import com.minhthien.web.coach.enums.SubscriptionPlanCode;
import com.minhthien.web.coach.enums.UserRole;
import com.minhthien.web.coach.enums.WalletTransactionType;
import com.minhthien.web.coach.enums.WalletWithdrawalStatus;
import com.minhthien.web.coach.exception.BadRequestException;
import com.minhthien.web.coach.exception.ResourceNotFoundException;
import com.minhthien.web.coach.repository.BookingRepository;
import com.minhthien.web.coach.repository.PlatformSettingsRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.repository.UserSubscriptionRepository;
import com.minhthien.web.coach.repository.WalletRepository;
import com.minhthien.web.coach.repository.WalletTransactionRepository;
import com.minhthien.web.coach.service.AdminApiService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminApiServiceImpl implements AdminApiService {

    private static final Long PLATFORM_SETTINGS_ID = 1L;

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletRepository walletRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PlatformSettingsRepository platformSettingsRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminApiResponses.AdminUserResponse> getUsers(UserRole role, String status, String keyword, int page, int size) {
        return userRepository.findAll(userSpec(role, status, keyword), pageRequest(page, size, "createdAt"))
                .map(this::mapUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminApiResponses.AdminUserResponse getUser(Long id) {
        return mapUser(getUserEntity(id));
    }

    @Override
    @Transactional
    public AdminApiResponses.AdminUserResponse updateUserStatus(Long id, AdminApiRequests.UserStatusRequest request, Boolean active) {
        Boolean resolvedActive = active != null ? active : request != null ? request.getActive() : null;
        if (resolvedActive == null) {
            throw new BadRequestException("active is required");
        }

        User user = getUserEntity(id);
        user.setActive(resolvedActive);
        return mapUser(userRepository.save(user));
    }

    @Override
    @Transactional
    public void softDeleteUser(Long id) {
        User user = getUserEntity(id);
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminApiResponses.DashboardOverviewResponse getDashboardOverview() {
        List<User> users = userRepository.findAll();
        List<Booking> bookings = bookingRepository.findAll();
        List<WalletTransaction> transactions = walletTransactionRepository.findAll();
        LocalDate today = LocalDate.now();

        return AdminApiResponses.DashboardOverviewResponse.builder()
                .totalUsers(users.size())
                .totalCoaches(countUsers(users, UserRole.COACHES))
                .totalTrainees(countUsers(users, UserRole.TRAINEES))
                .totalBookings(bookings.size())
                .pendingBookings(countBookings(bookings, BookingStatus.PENDING))
                .completedBookings(countBookings(bookings, BookingStatus.COMPLETED))
                .totalTransactions(transactions.size())
                .todayTransactions(countTransactions(transactions, today.atStartOfDay(), today.plusDays(1).atStartOfDay()))
                .todayRevenue(sumTransactions(transactions, today.atStartOfDay(), today.plusDays(1).atStartOfDay(), true))
                .monthRevenue(sumTransactions(transactions, today.withDayOfMonth(1).atStartOfDay(), today.plusDays(1).atStartOfDay(), true))
                .platformCommission(sumByType(transactions, WalletTransactionType.BOOKING_COMMISSION, null, null))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminApiResponses.RevenueChartPointResponse> getRevenueChart(String range) {
        int points = "week".equalsIgnoreCase(range) ? 7 : 6;
        LocalDate today = LocalDate.now();
        List<WalletTransaction> transactions = walletTransactionRepository.findAll();

        if ("week".equalsIgnoreCase(range)) {
            List<AdminApiResponses.RevenueChartPointResponse> result = new ArrayList<>();
            for (int i = points - 1; i >= 0; i--) {
                LocalDate day = today.minusDays(i);
                result.add(buildChartPoint(day.toString(), transactions, day.atStartOfDay(), day.plusDays(1).atStartOfDay()));
            }
            return result;
        }

        List<AdminApiResponses.RevenueChartPointResponse> result = new ArrayList<>();
        YearMonth currentMonth = YearMonth.from(today);
        for (int i = points - 1; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            result.add(buildChartPoint(month.toString(), transactions, month.atDay(1).atStartOfDay(), month.plusMonths(1).atDay(1).atStartOfDay()));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminApiResponses.AdminTransactionResponse> getRecentTransactions() {
        return walletTransactionRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(this::mapTransaction)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminApiResponses.AdminAlertResponse> getAlerts() {
        List<AdminApiResponses.AdminAlertResponse> alerts = new ArrayList<>();
        long pendingBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING)
                .count();
        if (pendingBookings > 0) {
            alerts.add(alert("BOOKING", "INFO", pendingBookings + " pending bookings need coach review"));
        }

        long pendingWithdrawals = walletTransactionRepository.findAll().stream()
                .filter(t -> t.getType() == WalletTransactionType.WITHDRAWAL)
                .filter(t -> t.getWithdrawalStatus() == WalletWithdrawalStatus.PROCESSING)
                .count();
        if (pendingWithdrawals > 0) {
            alerts.add(alert("WITHDRAWAL", "WARNING", pendingWithdrawals + " withdrawal requests are waiting for admin review"));
        }

        if (walletRepository.findByUserRole(UserRole.ADMIN).isEmpty()) {
            alerts.add(alert("WALLET", "WARNING", "Admin wallet has not been created yet"));
        }

        LocalDateTime now = LocalDateTime.now();
        long expiringSubscriptions = userSubscriptionRepository.findAll().stream()
                .filter(s -> Boolean.TRUE.equals(s.getActive()))
                .filter(s -> s.getExpiresAt() != null && s.getExpiresAt().isAfter(now) && s.getExpiresAt().isBefore(now.plusDays(7)))
                .count();
        if (expiringSubscriptions > 0) {
            alerts.add(alert("SUBSCRIPTION", "INFO", expiringSubscriptions + " subscriptions expire in the next 7 days"));
        }

        return alerts;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminApiResponses.AdminTransactionResponse> getTransactions(
            String status,
            SubscriptionPlanCode coachPlan,
            String keyword,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    ) {
        Pageable pageable = pageRequest(page, size, "createdAt");
        List<AdminApiResponses.AdminTransactionResponse> filtered = getFilteredTuitionTransactions(status, coachPlan, keyword, from, to);
        int start = Math.min((int) pageable.getOffset(), filtered.size());
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminApiResponses.TuitionTransactionSummaryResponse getTransactionSummary(
            String status,
            SubscriptionPlanCode coachPlan,
            String keyword,
            LocalDate from,
            LocalDate to
    ) {
        List<AdminApiResponses.AdminTransactionResponse> transactions = getFilteredTuitionTransactions(status, coachPlan, keyword, from, to);
        long totalAmount = transactions.stream().mapToLong(t -> nullToZero(t.getAmount())).sum();
        long totalCommission = transactions.stream().mapToLong(t -> nullToZero(t.getCommission())).sum();
        long totalCoachPayout = transactions.stream().mapToLong(t -> nullToZero(t.getCoachPayout())).sum();

        List<AdminApiResponses.TuitionCommissionByPlanResponse> breakdown = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getCoachPlanCode() == null ? SubscriptionPlanCode.FREE : t.getCoachPlanCode()))
                .entrySet()
                .stream()
                .map(entry -> buildTuitionPlanSummary(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(r -> r.getPlanCode().name()))
                .toList();

        return AdminApiResponses.TuitionTransactionSummaryResponse.builder()
                .totalAmount(totalAmount)
                .totalCommission(totalCommission)
                .totalCoachPayout(totalCoachPayout)
                .transactionCount(transactions.size())
                .averageCommissionRate(totalAmount > 0 ? totalCommission * 100.0d / totalAmount : 0.0d)
                .breakdownByPlan(breakdown)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminApiResponses.AdminTransactionResponse getTransaction(Long id) {
        WalletTransaction transaction = walletTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WalletTransaction", "id", id));
        return mapTransaction(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminApiResponses.AdminSubscriptionResponse> getSubscriptions(UserRole role, SubscriptionPlanCode plan, String status, String keyword, int page, int size) {
        return userSubscriptionRepository.findAll(subscriptionSpec(role, plan, status, keyword), pageRequest(page, size, "updatedAt"))
                .map(this::mapSubscription);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminApiResponses.SubscriptionStatsResponse getSubscriptionStats() {
        List<UserSubscription> subscriptions = userSubscriptionRepository.findAll();
        return AdminApiResponses.SubscriptionStatsResponse.builder()
                .totalSubscriptions(subscriptions.size())
                .activeSubscriptions(subscriptions.stream().filter(s -> Boolean.TRUE.equals(s.getActive())).count())
                .freePlans(countPlan(subscriptions, SubscriptionPlanCode.FREE))
                .proPlans(countPlan(subscriptions, SubscriptionPlanCode.PRO))
                .premiumPlans(countPlan(subscriptions, SubscriptionPlanCode.PREMIUM))
                .monthlyPlans(countBillingCycle(subscriptions, SubscriptionBillingCycle.MONTHLY))
                .yearlyPlans(countBillingCycle(subscriptions, SubscriptionBillingCycle.YEARLY))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminApiResponses.SubscriptionSummaryResponse getSubscriptionSummary() {
        List<UserSubscription> subscriptions = userSubscriptionRepository.findAll();
        long totalMonthlyRevenue = subscriptions.stream()
                .filter(this::isActiveSubscription)
                .mapToLong(s -> nullToZero(s.getMonthlyPriceSnapshot()))
                .sum();

        return AdminApiResponses.SubscriptionSummaryResponse.builder()
                .learnerPlans(buildSubscriptionPlanSummaries(subscriptions, UserRole.TRAINEES))
                .coachPlans(buildSubscriptionPlanSummaries(subscriptions, UserRole.COACHES))
                .revenueRows(buildSubscriptionRevenueRows(subscriptions))
                .renewalAlerts(buildSubscriptionRenewalAlerts(subscriptions))
                .totalMonthlyRevenue(totalMonthlyRevenue)
                .activeSubscriptions(subscriptions.stream().filter(this::isActiveSubscription).count())
                .expiredSubscriptions(subscriptions.stream().filter(this::isExpiredSubscription).count())
                .build();
    }

    @Override
    @Transactional
    public AdminApiResponses.AdminSubscriptionResponse updateSubscription(Long userId, AdminApiRequests.SubscriptionUpdateRequest request) {
        User user = getUserEntity(userId);
        if (user.getRole() != UserRole.TRAINEES && user.getRole() != UserRole.COACHES) {
            throw new BadRequestException("This user role does not support subscriptions");
        }

        UserSubscription subscription = userSubscriptionRepository.findByUserId(userId)
                .orElseGet(() -> UserSubscription.builder()
                        .user(user)
                        .planCode(SubscriptionPlanCode.FREE)
                        .billingCycle(SubscriptionBillingCycle.MONTHLY)
                        .active(true)
                        .startedAt(LocalDateTime.now())
                        .build());

        SubscriptionPlanCode planCode = request != null && request.getPlanCode() != null
                ? request.getPlanCode()
                : subscription.getPlanCode();
        SubscriptionBillingCycle billingCycle = request != null && request.getBillingCycle() != null
                ? request.getBillingCycle()
                : subscription.getBillingCycle();

        if (user.getRole() == UserRole.TRAINEES && planCode == SubscriptionPlanCode.PREMIUM) {
            throw new BadRequestException("Trainee accounts only support FREE and PRO plans");
        }

        subscription.setPlanCode(planCode);
        subscription.setBillingCycle(billingCycle == null ? SubscriptionBillingCycle.MONTHLY : billingCycle);
        subscription.setActive(request == null || request.getActive() == null ? subscription.getActive() : request.getActive());
        subscription.setExpiresAt(request == null ? subscription.getExpiresAt() : request.getExpiresAt());
        if (subscription.getStartedAt() == null) {
            subscription.setStartedAt(LocalDateTime.now());
        }

        Long monthlyPrice = getMonthlyPrice(user.getRole(), subscription.getPlanCode());
        subscription.setMonthlyPriceSnapshot(monthlyPrice);
        subscription.setBillingPriceSnapshot(calculateBillingPrice(monthlyPrice, subscription.getBillingCycle()));

        return mapSubscription(userSubscriptionRepository.save(subscription));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminApiResponses.FinanceOverviewResponse getFinanceOverview() {
        List<WalletTransaction> transactions = walletTransactionRepository.findAll();
        LocalDate today = LocalDate.now();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime weekStart = today.minusDays(6).atStartOfDay();
        LocalDateTime tomorrow = today.plusDays(1).atStartOfDay();

        long subscriptionRevenue = sumByType(transactions, WalletTransactionType.SUBSCRIPTION_REVENUE, null, null);
        long bookingRevenue = sumByType(transactions, WalletTransactionType.BOOKING_COMMISSION, null, null)
                + sumByType(transactions, WalletTransactionType.BOOKING_COACH_PAYOUT, null, null);

        return AdminApiResponses.FinanceOverviewResponse.builder()
                .totalRevenue(subscriptionRevenue + bookingRevenue)
                .monthRevenue(sumTransactions(transactions, monthStart, tomorrow, true))
                .weekRevenue(sumTransactions(transactions, weekStart, tomorrow, true))
                .subscriptionRevenue(subscriptionRevenue)
                .bookingRevenue(bookingRevenue)
                .platformCommission(sumByType(transactions, WalletTransactionType.BOOKING_COMMISSION, null, null))
                .coachPayout(sumByType(transactions, WalletTransactionType.BOOKING_COACH_PAYOUT, null, null))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminApiResponses.RevenueChartPointResponse> getMonthlyRevenue() {
        return getRevenueChart("month");
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminApiResponses.CommissionByPlanResponse> getCommissionByPlan() {
        return getFilteredTuitionTransactions("SUCCESS", null, null, null, null).stream()
                .collect(Collectors.groupingBy(t -> t.getCoachPlanCode() == null ? SubscriptionPlanCode.FREE : t.getCoachPlanCode()))
                .entrySet()
                .stream()
                .map(entry -> AdminApiResponses.CommissionByPlanResponse.builder()
                        .planCode(entry.getKey())
                        .commission(entry.getValue().stream().mapToLong(t -> nullToZero(t.getCommission())).sum())
                        .transactionCount(entry.getValue().size())
                        .build())
                .sorted(Comparator.comparing(r -> r.getPlanCode().name()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminApiResponses.RevenueBySourceResponse> getRevenueBySource() {
        List<WalletTransaction> transactions = walletTransactionRepository.findAll();
        return List.of(
                revenueSource("SUBSCRIPTION", transactions, WalletTransactionType.SUBSCRIPTION_REVENUE),
                revenueSource("BOOKING_COMMISSION", transactions, WalletTransactionType.BOOKING_COMMISSION),
                revenueSource("TOP_UP", transactions, WalletTransactionType.TOP_UP)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminApiResponses.TopCoachResponse> getTopCoaches() {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getCoach() != null && b.getCoach().getUser() != null)
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .collect(Collectors.groupingBy(b -> b.getCoach().getId()))
                .values()
                .stream()
                .map(this::mapTopCoach)
                .sorted(Comparator.comparingLong(AdminApiResponses.TopCoachResponse::getRevenue).reversed())
                .limit(10)
                .toList();
    }

    private List<AdminApiResponses.AdminTransactionResponse> getFilteredTuitionTransactions(
            String status,
            SubscriptionPlanCode coachPlan,
            String keyword,
            LocalDate from,
            LocalDate to
    ) {
        return walletTransactionRepository.findAll(transactionSpec(status, from, to), Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .filter(this::isTuitionTransaction)
                .map(this::mapTransaction)
                .filter(transaction -> coachPlan == null || coachPlan == transaction.getCoachPlanCode())
                .filter(transaction -> matchesTransactionKeyword(transaction, keyword))
                .toList();
    }

    private boolean isTuitionTransaction(WalletTransaction transaction) {
        return transaction.getType() == WalletTransactionType.BOOKING_PAYMENT;
    }

    private boolean matchesTransactionKeyword(AdminApiResponses.AdminTransactionResponse transaction, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
        return containsIgnoreCase("TXN-" + transaction.getId(), normalizedKeyword)
                || containsIgnoreCase(transaction.getLearnerName(), normalizedKeyword)
                || containsIgnoreCase(transaction.getCoachName(), normalizedKeyword)
                || containsIgnoreCase(transaction.getUserName(), normalizedKeyword)
                || containsIgnoreCase(transaction.getReferenceId(), normalizedKeyword)
                || containsIgnoreCase(transaction.getDescription(), normalizedKeyword);
    }

    private boolean containsIgnoreCase(String value, String normalizedKeyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedKeyword);
    }

    private AdminApiResponses.TuitionCommissionByPlanResponse buildTuitionPlanSummary(
            SubscriptionPlanCode planCode,
            List<AdminApiResponses.AdminTransactionResponse> transactions
    ) {
        long totalTuition = transactions.stream().mapToLong(t -> nullToZero(t.getAmount())).sum();
        long commission = transactions.stream().mapToLong(t -> nullToZero(t.getCommission())).sum();
        long coachPayout = transactions.stream().mapToLong(t -> nullToZero(t.getCoachPayout())).sum();
        Integer commissionRate = transactions.stream()
                .map(AdminApiResponses.AdminTransactionResponse::getCommissionRate)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(resolveCommissionRate(planCode));

        return AdminApiResponses.TuitionCommissionByPlanResponse.builder()
                .planCode(planCode)
                .planName(planName(planCode))
                .commissionRate(commissionRate)
                .transactionCount(transactions.size())
                .totalTuition(totalTuition)
                .commission(commission)
                .coachPayout(coachPayout)
                .build();
    }

    private Specification<User> userSpec(UserRole role, String status, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (role != null) {
                predicates.add(cb.equal(root.get("role"), role));
            }
            if (StringUtils.hasText(status)) {
                String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);
                if ("ACTIVE".equals(normalizedStatus)) {
                    predicates.add(cb.isTrue(root.get("active")));
                } else if ("INACTIVE".equals(normalizedStatus) || "SUSPENDED".equals(normalizedStatus)) {
                    predicates.add(cb.isFalse(root.get("active")));
                }
            }
            if (StringUtils.hasText(keyword)) {
                String likeKeyword = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("username")), likeKeyword),
                        cb.like(cb.lower(root.get("email")), likeKeyword),
                        cb.like(cb.lower(root.get("fullName")), likeKeyword)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<WalletTransaction> transactionSpec(String status, LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay()));
            }
            if (to != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), to.plusDays(1).atStartOfDay()));
            }
            if (StringUtils.hasText(status)) {
                if ("SUCCESS".equalsIgnoreCase(status)) {
                    predicates.add(cb.or(
                            cb.notEqual(root.get("type"), WalletTransactionType.WITHDRAWAL),
                            cb.equal(root.get("withdrawalStatus"), WalletWithdrawalStatus.COMPLETED)
                    ));
                } else {
                    WalletWithdrawalStatus withdrawalStatus = parseWithdrawalStatus(status);
                    if (withdrawalStatus != null) {
                        predicates.add(cb.equal(root.get("withdrawalStatus"), withdrawalStatus));
                    }
                }
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<UserSubscription> subscriptionSpec(UserRole role, SubscriptionPlanCode plan, String status, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<UserSubscription, User> user = root.join("user");
            if (role != null) {
                predicates.add(cb.equal(user.get("role"), role));
            }
            if (plan != null) {
                predicates.add(cb.equal(root.get("planCode"), plan));
            }
            if (StringUtils.hasText(status)) {
                String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);
                LocalDateTime now = LocalDateTime.now();
                if ("ACTIVE".equals(normalizedStatus)) {
                    predicates.add(cb.isTrue(root.get("active")));
                    predicates.add(cb.or(
                            cb.isNull(root.get("expiresAt")),
                            cb.greaterThanOrEqualTo(root.get("expiresAt"), now)
                    ));
                } else if ("EXPIRED".equals(normalizedStatus)) {
                    predicates.add(cb.lessThan(root.get("expiresAt"), now));
                } else if ("INACTIVE".equals(normalizedStatus)) {
                    predicates.add(cb.isFalse(root.get("active")));
                }
            }
            if (StringUtils.hasText(keyword)) {
                String likeKeyword = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(user.get("username")), likeKeyword),
                        cb.like(cb.lower(user.get("email")), likeKeyword),
                        cb.like(cb.lower(user.get("fullName")), likeKeyword)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private List<AdminApiResponses.SubscriptionPlanSummaryResponse> buildSubscriptionPlanSummaries(
            List<UserSubscription> subscriptions,
            UserRole role
    ) {
        List<UserSubscription> roleSubscriptions = subscriptions.stream()
                .filter(subscription -> subscription.getUser() != null && subscription.getUser().getRole() == role)
                .toList();
        long total = roleSubscriptions.size();

        return List.of(SubscriptionPlanCode.FREE, SubscriptionPlanCode.PRO, SubscriptionPlanCode.PREMIUM)
                .stream()
                .map(planCode -> {
                    long count = roleSubscriptions.stream().filter(subscription -> subscription.getPlanCode() == planCode).count();
                    return AdminApiResponses.SubscriptionPlanSummaryResponse.builder()
                            .role(role)
                            .planCode(planCode)
                            .planName(subscriptionPlanName(role, planCode))
                            .count(count)
                            .percentage(total > 0 ? count * 100.0d / total : 0.0d)
                            .build();
                })
                .toList();
    }

    private List<AdminApiResponses.SubscriptionRevenueSummaryResponse> buildSubscriptionRevenueRows(List<UserSubscription> subscriptions) {
        return subscriptions.stream()
                .filter(this::isActiveSubscription)
                .filter(subscription -> nullToZero(subscription.getMonthlyPriceSnapshot()) > 0)
                .collect(Collectors.groupingBy(subscription -> subscription.getUser().getRole() + ":" + subscription.getPlanCode()))
                .values()
                .stream()
                .map(group -> {
                    UserSubscription first = group.get(0);
                    UserRole role = first.getUser().getRole();
                    SubscriptionPlanCode planCode = first.getPlanCode();
                    long monthlyPrice = nullToZero(first.getMonthlyPriceSnapshot());
                    return AdminApiResponses.SubscriptionRevenueSummaryResponse.builder()
                            .role(role)
                            .planCode(planCode)
                            .planName(subscriptionPlanName(role, planCode))
                            .count(group.size())
                            .monthlyPrice(monthlyPrice)
                            .revenue(group.stream().mapToLong(s -> nullToZero(s.getMonthlyPriceSnapshot())).sum())
                            .build();
                })
                .sorted(Comparator.comparing((AdminApiResponses.SubscriptionRevenueSummaryResponse row) -> row.getRole().name())
                        .thenComparing(row -> row.getPlanCode().name()))
                .toList();
    }

    private List<AdminApiResponses.SubscriptionRenewalAlertResponse> buildSubscriptionRenewalAlerts(List<UserSubscription> subscriptions) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextWeek = now.plusDays(7);
        return subscriptions.stream()
                .filter(this::isActiveSubscription)
                .filter(subscription -> subscription.getExpiresAt() != null)
                .filter(subscription -> !subscription.getExpiresAt().isBefore(now) && subscription.getExpiresAt().isBefore(nextWeek))
                .collect(Collectors.groupingBy(subscription -> subscription.getUser().getRole() + ":" + subscription.getPlanCode()))
                .values()
                .stream()
                .map(group -> {
                    UserSubscription first = group.get(0);
                    UserRole role = first.getUser().getRole();
                    SubscriptionPlanCode planCode = first.getPlanCode();
                    return AdminApiResponses.SubscriptionRenewalAlertResponse.builder()
                            .role(role)
                            .planCode(planCode)
                            .planName(subscriptionPlanName(role, planCode))
                            .count(group.size())
                            .build();
                })
                .sorted(Comparator.comparing((AdminApiResponses.SubscriptionRenewalAlertResponse row) -> row.getRole().name())
                        .thenComparing(row -> row.getPlanCode().name()))
                .toList();
    }

    private boolean isActiveSubscription(UserSubscription subscription) {
        return Boolean.TRUE.equals(subscription.getActive())
                && (subscription.getExpiresAt() == null || !subscription.getExpiresAt().isBefore(LocalDateTime.now()));
    }

    private boolean isExpiredSubscription(UserSubscription subscription) {
        return subscription.getExpiresAt() != null && subscription.getExpiresAt().isBefore(LocalDateTime.now());
    }

    private String subscriptionStatus(UserSubscription subscription) {
        if (isExpiredSubscription(subscription)) {
            return "EXPIRED";
        }
        if (!Boolean.TRUE.equals(subscription.getActive())) {
            return "INACTIVE";
        }
        return "ACTIVE";
    }

    private String subscriptionPlanName(UserRole role, SubscriptionPlanCode planCode) {
        if (role == UserRole.COACHES) {
            return planName(planCode);
        }
        return switch (planCode == null ? SubscriptionPlanCode.FREE : planCode) {
            case FREE -> "Free";
            case PRO -> "Pro";
            case PREMIUM -> "Premium";
        };
    }

    private String resolveUserSubscriptionPlanName(User user) {
        if (user.getRole() != UserRole.TRAINEES && user.getRole() != UserRole.COACHES) {
            return "-";
        }
        SubscriptionPlanCode planCode = userSubscriptionRepository.findByUserId(user.getId())
                .map(UserSubscription::getPlanCode)
                .orElse(SubscriptionPlanCode.FREE);
        return subscriptionPlanName(user.getRole(), planCode);
    }

    private AdminApiResponses.AdminUserResponse mapUser(User user) {
        Long totalSessions = 0L;
        Long totalSpent = 0L;
        Long totalStudents = 0L;
        Long totalRevenue = 0L;

        if (user.getRole() == UserRole.TRAINEES) {
            totalSessions = bookingRepository.countByTraineeId(user.getId());
            totalSpent = nullToZero(bookingRepository.sumSettledAmountByTraineeId(user.getId()));
        } else if (user.getRole() == UserRole.COACHES) {
            totalSessions = nullToZero(bookingRepository.countByCoachUserId(user.getId()));
            totalStudents = nullToZero(bookingRepository.countStudentsByCoachUserId(user.getId()));
            totalRevenue = nullToZero(bookingRepository.sumCoachPayoutByCoachUserId(user.getId()));
        }

        return AdminApiResponses.AdminUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .active(user.getActive())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .subscriptionPlanName(resolveUserSubscriptionPlanName(user))
                .totalSessions(totalSessions)
                .totalSpent(totalSpent)
                .totalStudents(totalStudents)
                .totalRevenue(totalRevenue)
                .build();
    }

    private AdminApiResponses.AdminTransactionResponse mapTransaction(WalletTransaction transaction) {
        User user = transaction.getWallet() == null ? null : transaction.getWallet().getUser();
        Booking booking = findReferencedBooking(transaction);
        User coachUser = booking == null || booking.getCoach() == null ? null : booking.getCoach().getUser();
        SubscriptionPlanCode coachPlanCode = resolveCoachPlanCode(coachUser);
        return AdminApiResponses.AdminTransactionResponse.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .userId(user == null ? null : user.getId())
                .userName(user == null ? null : user.getFullName())
                .learnerName(booking == null || booking.getTrainee() == null ? null : booking.getTrainee().getFullName())
                .coachName(coachUser == null ? null : coachUser.getFullName())
                .bookingId(booking == null ? null : booking.getId())
                .bookingType(booking == null || booking.getType() == null ? null : booking.getType().name())
                .coachPlanCode(coachPlanCode)
                .coachPlanName(planName(coachPlanCode))
                .commissionRate(resolveCommissionRate(coachPlanCode))
                .amount(Math.abs(nullToZero(transaction.getAmount())))
                .commission(resolveTransactionCommission(transaction, booking))
                .coachPayout(booking == null ? null : booking.getCoachPayoutAmount())
                .status(resolveTransactionStatus(transaction))
                .description(transaction.getDescription())
                .referenceType(transaction.getReferenceType())
                .referenceId(transaction.getReferenceId())
                .subscriptionPlanCode(transaction.getSubscriptionPlanCode())
                .subscriptionBillingCycle(transaction.getSubscriptionBillingCycle())
                .withdrawalStatus(transaction.getWithdrawalStatus())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    private AdminApiResponses.AdminSubscriptionResponse mapSubscription(UserSubscription subscription) {
        User user = subscription.getUser();
        return AdminApiResponses.AdminSubscriptionResponse.builder()
                .id(subscription.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .planCode(subscription.getPlanCode())
                .planName(subscriptionPlanName(user.getRole(), subscription.getPlanCode()))
                .billingCycle(subscription.getBillingCycle())
                .active(subscription.getActive())
                .status(subscriptionStatus(subscription))
                .monthlyPrice(subscription.getMonthlyPriceSnapshot())
                .billingPrice(subscription.getBillingPriceSnapshot())
                .startedAt(subscription.getStartedAt())
                .expiresAt(subscription.getExpiresAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }

    private AdminApiResponses.RevenueChartPointResponse buildChartPoint(
            String period,
            List<WalletTransaction> transactions,
            LocalDateTime start,
            LocalDateTime end
    ) {
        long bookingCommission = sumByType(transactions, WalletTransactionType.BOOKING_COMMISSION, start, end);
        long bookingPayout = sumByType(transactions, WalletTransactionType.BOOKING_COACH_PAYOUT, start, end);
        long subscriptionRevenue = sumByType(transactions, WalletTransactionType.SUBSCRIPTION_REVENUE, start, end);
        long bookingRevenue = bookingCommission + bookingPayout;

        return AdminApiResponses.RevenueChartPointResponse.builder()
                .period(period)
                .revenue(bookingRevenue + subscriptionRevenue)
                .bookingRevenue(bookingRevenue)
                .subscriptionRevenue(subscriptionRevenue)
                .commission(bookingCommission)
                .build();
    }

    private AdminApiResponses.AdminAlertResponse alert(String type, String severity, String message) {
        return AdminApiResponses.AdminAlertResponse.builder()
                .type(type)
                .severity(severity)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private AdminApiResponses.RevenueBySourceResponse revenueSource(String source, List<WalletTransaction> transactions, WalletTransactionType type) {
        List<WalletTransaction> sourceTransactions = transactions.stream()
                .filter(t -> t.getType() == type)
                .toList();
        return AdminApiResponses.RevenueBySourceResponse.builder()
                .source(source)
                .revenue(sourceTransactions.stream().mapToLong(t -> Math.abs(nullToZero(t.getAmount()))).sum())
                .transactionCount(sourceTransactions.size())
                .build();
    }

    private AdminApiResponses.TopCoachResponse mapTopCoach(List<Booking> bookings) {
        Booking first = bookings.get(0);
        long revenue = bookings.stream().mapToLong(this::bookingTotalAmount).sum();
        long payout = bookings.stream().mapToLong(b -> nullToZero(b.getCoachPayoutAmount())).sum();
        return AdminApiResponses.TopCoachResponse.builder()
                .coachId(first.getCoach().getId())
                .coachName(first.getCoach().getUser().getFullName())
                .completedBookings(bookings.size())
                .revenue(revenue)
                .payout(payout)
                .build();
    }

    private User getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    private Pageable pageRequest(int page, int size, String sortField) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 20 : Math.min(size, 100);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, sortField));
    }

    private long countUsers(List<User> users, UserRole role) {
        return users.stream().filter(user -> user.getRole() == role).count();
    }

    private long countBookings(List<Booking> bookings, BookingStatus status) {
        return bookings.stream().filter(booking -> booking.getStatus() == status).count();
    }

    private long countPlan(List<UserSubscription> subscriptions, SubscriptionPlanCode planCode) {
        return subscriptions.stream().filter(subscription -> subscription.getPlanCode() == planCode).count();
    }

    private long countBillingCycle(List<UserSubscription> subscriptions, SubscriptionBillingCycle billingCycle) {
        return subscriptions.stream().filter(subscription -> subscription.getBillingCycle() == billingCycle).count();
    }

    private long sumTransactions(List<WalletTransaction> transactions, LocalDateTime start, LocalDateTime end, boolean revenueOnly) {
        return transactions.stream()
                .filter(t -> !t.getCreatedAt().isBefore(start) && t.getCreatedAt().isBefore(end))
                .filter(t -> !revenueOnly || t.getType() == WalletTransactionType.SUBSCRIPTION_REVENUE
                        || t.getType() == WalletTransactionType.BOOKING_COMMISSION
                        || t.getType() == WalletTransactionType.BOOKING_COACH_PAYOUT)
                .mapToLong(t -> Math.abs(nullToZero(t.getAmount())))
                .sum();
    }

    private long countTransactions(List<WalletTransaction> transactions, LocalDateTime start, LocalDateTime end) {
        return transactions.stream()
                .filter(t -> !t.getCreatedAt().isBefore(start) && t.getCreatedAt().isBefore(end))
                .count();
    }

    private long sumByType(List<WalletTransaction> transactions, WalletTransactionType type, LocalDateTime start, LocalDateTime end) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .filter(t -> start == null || !t.getCreatedAt().isBefore(start))
                .filter(t -> end == null || t.getCreatedAt().isBefore(end))
                .mapToLong(t -> Math.abs(nullToZero(t.getAmount())))
                .sum();
    }

    private Booking findReferencedBooking(WalletTransaction transaction) {
        if (!"BOOKING".equalsIgnoreCase(transaction.getReferenceType()) || !StringUtils.hasText(transaction.getReferenceId())) {
            return null;
        }
        try {
            return bookingRepository.findById(Long.valueOf(transaction.getReferenceId())).orElse(null);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long resolveTransactionCommission(WalletTransaction transaction, Booking booking) {
        if (transaction.getType() == WalletTransactionType.BOOKING_COMMISSION) {
            return Math.abs(nullToZero(transaction.getAmount()));
        }
        return booking == null ? null : booking.getAdminCommissionAmount();
    }

    private String resolveTransactionStatus(WalletTransaction transaction) {
        if (transaction.getType() == WalletTransactionType.WITHDRAWAL) {
            return transaction.getWithdrawalStatus() == null ? "PROCESSING" : transaction.getWithdrawalStatus().name();
        }
        return "SUCCESS";
    }

    private WalletWithdrawalStatus parseWithdrawalStatus(String status) {
        try {
            return WalletWithdrawalStatus.valueOf(status.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private SubscriptionPlanCode resolveCoachPlanCode(User coachUser) {
        if (coachUser == null) {
            return SubscriptionPlanCode.FREE;
        }
        return userSubscriptionRepository.findByUserId(coachUser.getId())
                .map(UserSubscription::getPlanCode)
                .orElse(SubscriptionPlanCode.FREE);
    }

    private String planName(SubscriptionPlanCode planCode) {
        if (planCode == null) {
            return "Starter";
        }
        return switch (planCode) {
            case FREE -> "Starter";
            case PRO -> "Pro Coach";
            case PREMIUM -> "Elite Coach";
        };
    }

    private int resolveCommissionRate(SubscriptionPlanCode planCode) {
        PlatformSettings settings = platformSettingsRepository.findById(PLATFORM_SETTINGS_ID).orElse(null);
        SubscriptionPlanCode resolvedPlanCode = planCode == null ? SubscriptionPlanCode.FREE : planCode;
        if (settings == null) {
            return switch (resolvedPlanCode) {
                case FREE -> 20;
                case PRO -> 12;
                case PREMIUM -> 0;
            };
        }
        return switch (resolvedPlanCode) {
            case FREE -> settings.getStarterCommissionRate();
            case PRO -> settings.getProCoachCommissionRate();
            case PREMIUM -> settings.getEliteCoachCommissionRate();
        };
    }

    private Long getMonthlyPrice(UserRole role, SubscriptionPlanCode planCode) {
        PlatformSettings settings = platformSettingsRepository.findById(PLATFORM_SETTINGS_ID).orElse(null);
        if (settings == null || planCode == null) {
            return 0L;
        }
        if (role == UserRole.TRAINEES) {
            return switch (planCode) {
                case FREE -> settings.getTraineeFreePrice();
                case PRO -> settings.getTraineeProPrice();
                case PREMIUM -> settings.getTraineePremiumPrice();
            };
        }
        if (role == UserRole.COACHES) {
            return switch (planCode) {
                case FREE -> settings.getCoachStarterPrice();
                case PRO -> settings.getCoachProPrice();
                case PREMIUM -> settings.getCoachElitePrice();
            };
        }
        return 0L;
    }

    private Long calculateBillingPrice(Long monthlyPrice, SubscriptionBillingCycle billingCycle) {
        if (monthlyPrice == null || monthlyPrice <= 0) {
            return 0L;
        }
        return billingCycle == SubscriptionBillingCycle.YEARLY ? monthlyPrice * 10 : monthlyPrice;
    }

    private long bookingTotalAmount(Booking booking) {
        if (booking.getSettledAmount() != null) {
            return booking.getSettledAmount();
        }
        if (booking.getPrice() == null) {
            return 0L;
        }
        return Math.round(booking.getPrice());
    }

    private long nullToZero(Long value) {
        return Objects.requireNonNullElse(value, 0L);
    }
}
