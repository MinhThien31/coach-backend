package com.minhthien.web.coach.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhthien.web.coach.dto.request.WalletBankAccountUpsertRequest;
import com.minhthien.web.coach.dto.request.WalletTopUpRequest;
import com.minhthien.web.coach.dto.request.WalletWithdrawRequest;
import com.minhthien.web.coach.dto.request.WalletWithdrawalReviewRequest;
import com.minhthien.web.coach.dto.response.*;
import com.minhthien.web.coach.entity.*;
import com.minhthien.web.coach.enums.SubscriptionBillingCycle;
import com.minhthien.web.coach.enums.SubscriptionPlanCode;
import com.minhthien.web.coach.enums.UserRole;
import com.minhthien.web.coach.enums.WalletTopUpOrderStatus;
import com.minhthien.web.coach.enums.WalletTransactionType;
import com.minhthien.web.coach.enums.WalletWithdrawalStatus;
import com.minhthien.web.coach.exception.BadRequestException;
import com.minhthien.web.coach.exception.ResourceNotFoundException;
import com.minhthien.web.coach.repository.*;
import com.minhthien.web.coach.service.PayOSGatewayService;
import com.minhthien.web.coach.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private static final Long PLATFORM_SETTINGS_ID = 1L;

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final UserBankAccountRepository userBankAccountRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletTopUpOrderRepository walletTopUpOrderRepository;
    private final PlatformSettingsRepository platformSettingsRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PayOSGatewayService payOSGatewayService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public WalletResponse getMyWallet(Long currentUserId) {
        User user = getUser(currentUserId);
        return mapWallet(getOrCreateWallet(user));
    }

    @Override
    @Transactional
    public WalletTopUpResponse createTopUpPayment(Long currentUserId, WalletTopUpRequest request) {
        User user = getUser(currentUserId);
        if (user.getRole() == UserRole.ADMIN) {
            throw new BadRequestException("Admin wallet chỉ nhận tiền từ hệ thống, không hỗ trợ nạp trực tiếp");
        }

        Wallet wallet = getOrCreateWallet(user);
        Long orderCode = generateOrderCode();
        String description = buildTopUpDescription(orderCode);

        WalletTopUpOrder order = walletTopUpOrderRepository.save(
                WalletTopUpOrder.builder()
                        .user(user)
                        .wallet(wallet)
                        .orderCode(orderCode)
                        .amount(request.getAmount())
                        .description(description)
                        .status(WalletTopUpOrderStatus.PENDING)
                        .build()
        );

        try {
            PayOSGatewayService.PaymentLinkData paymentLink = payOSGatewayService.createPaymentLink(
                    orderCode,
                    request.getAmount(),
                    description,
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhone()
            );

            order.setPaymentLinkId(paymentLink.paymentLinkId());
            order.setCheckoutUrl(paymentLink.checkoutUrl());
            order.setQrCode(paymentLink.qrCode());
            WalletTopUpOrderStatus resolvedStatus = resolveTopUpStatus(paymentLink.status());
            if (resolvedStatus == WalletTopUpOrderStatus.PAID) {
                creditWalletFromTopUpOrder(order);
            } else {
                order.setStatus(resolvedStatus);
                walletTopUpOrderRepository.save(order);
            }

            return mapTopUp(order, wallet.getBalance());
        } catch (RuntimeException ex) {
            order.setStatus(WalletTopUpOrderStatus.FAILED);
            walletTopUpOrderRepository.save(order);
            throw ex;
        }
    }

    @Override
    @Transactional
    public WalletTopUpResponse getTopUpStatus(Long currentUserId, Long orderCode) {
        User user = getUser(currentUserId);
        WalletTopUpOrder order = walletTopUpOrderRepository.findByOrderCodeAndUserId(orderCode, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("WalletTopUpOrder", "orderCode", orderCode));

        if (order.getStatus() == WalletTopUpOrderStatus.PENDING) {
            PayOSGatewayService.PaymentRequestStatusData statusData = payOSGatewayService.getPaymentRequestStatus(orderCode);
            syncTopUpOrderFromPayOS(order, statusData);
        }

        Wallet wallet = getOrCreateWallet(user);
        return mapTopUp(order, wallet.getBalance());
    }


    @Override
    @Transactional
    public WalletBankAccountResponse upsertMyBankAccount(Long currentUserId, WalletBankAccountUpsertRequest request) {
        User user = getUser(currentUserId);
        UserBankAccount bankAccount = userBankAccountRepository.findByUserId(user.getId())
                .orElseGet(() -> UserBankAccount.builder().user(user).build());

        bankAccount.setBankCode(normalizeText(request.getBankCode()));
        bankAccount.setBankName(normalizeText(request.getBankName()));
        bankAccount.setAccountNumber(normalizeDigits(request.getAccountNumber()));
        bankAccount.setAccountHolderName(normalizeText(request.getAccountHolderName()));
        bankAccount.setBranch(normalizeNullableText(request.getBranch()));

        return mapBankAccount(userBankAccountRepository.save(bankAccount));
    }

    @Override
    @Transactional(readOnly = true)
    public WalletBankAccountResponse getMyBankAccount(Long currentUserId) {
        UserBankAccount bankAccount = userBankAccountRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserBankAccount", "userId", currentUserId));
        return mapBankAccount(bankAccount);
    }

    @Override
    @Transactional
    public WalletWithdrawResponse withdrawFromMyWallet(Long currentUserId, WalletWithdrawRequest request) {
        User user = getUser(currentUserId);
        Wallet wallet = getOrCreateWallet(user);
        UserBankAccount bankAccount = userBankAccountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Vui lòng thêm thông tin ngân hàng trước khi rút tiền"));

        String referenceId = String.valueOf(System.currentTimeMillis());
        String description = StringUtils.hasText(request.getNote())
                ? request.getNote().trim()
                : "Yêu cầu rút tiền từ ví của " + user.getRole().name();

        WalletTransaction transaction = applyTransaction(
                wallet,
                -request.getAmount(),
                WalletTransactionType.WITHDRAWAL,
                description,
                "WITHDRAWAL",
                referenceId,
                bankAccount
        );
        transaction.setWithdrawalStatus(WalletWithdrawalStatus.PROCESSING);
        transaction = walletTransactionRepository.save(transaction);

        walletRepository.save(wallet);
        return mapWithdrawResponse(transaction);
    }

    @Override
    @Transactional
    public void handlePayOSWebhook(Map<String, Object> payload) {
        PayOSGatewayService.VerifiedWebhookData webhookData = payOSGatewayService.verifyWebhook(payload);
        if (webhookData.orderCode() == null) {
            throw new BadRequestException("Webhook PayOS thiếu orderCode");
        }

        WalletTopUpOrder order = walletTopUpOrderRepository.findByOrderCode(webhookData.orderCode())
                .orElseThrow(() -> new ResourceNotFoundException("WalletTopUpOrder", "orderCode", webhookData.orderCode()));

        order.setPaymentLinkId(firstNonBlank(order.getPaymentLinkId(), webhookData.paymentLinkId()));
        order.setPayosReference(webhookData.reference());
        order.setPayosCode(webhookData.code());
        order.setRawWebhookPayload(writeJson(payload));

        if (order.getStatus() == WalletTopUpOrderStatus.PAID) {
            walletTopUpOrderRepository.save(order);
            return;
        }

        if (!webhookData.success() || !"00".equals(webhookData.code())) {
            order.setStatus(WalletTopUpOrderStatus.FAILED);
            walletTopUpOrderRepository.save(order);
            return;
        }

        if (!order.getAmount().equals(webhookData.amount())) {
            throw new BadRequestException("Số tiền webhook PayOS không khớp với top-up order");
        }

        creditWalletFromTopUpOrder(order);
    }

    @Override
    @Transactional
    public List<WalletTransactionResponse> getMyTransactions(Long currentUserId) {
        User user = getUser(currentUserId);
        Wallet wallet = getOrCreateWallet(user);
        return walletTransactionRepository.findTop50ByWalletIdOrderByCreatedAtDesc(wallet.getId())
                .stream()
                .map(this::mapTransaction)
                .toList();
    }

    @Override
    @Transactional
    public AdminWalletOverviewResponse getAdminWalletOverview() {
        User admin = getAdminUser();
        Wallet adminWallet = getOrCreateWallet(admin);
        return AdminWalletOverviewResponse.builder()
                .wallet(mapWallet(adminWallet))
                .totalTransactions(walletTransactionRepository.countByWalletId(adminWallet.getId()))
                .recentTransactions(walletTransactionRepository.findTop50ByWalletIdOrderByCreatedAtDesc(adminWallet.getId())
                        .stream()
                        .map(this::mapTransaction)
                        .toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminWalletWithdrawRequestResponse> getAdminWithdrawRequests(WalletWithdrawalStatus status) {
        List<WalletTransaction> transactions = status == null
                ? walletTransactionRepository.findTop100ByTypeOrderByCreatedAtDesc(WalletTransactionType.WITHDRAWAL)
                : walletTransactionRepository.findTop100ByTypeAndWithdrawalStatusOrderByCreatedAtDesc(
                WalletTransactionType.WITHDRAWAL,
                status
        );

        return transactions.stream()
                .map(this::mapAdminWithdrawRequest)
                .toList();
    }

    @Override
    @Transactional
    public AdminWalletWithdrawRequestResponse approveWithdrawRequest(Long adminUserId, Long transactionId, WalletWithdrawalReviewRequest request) {
        User admin = getAdminActor(adminUserId);
        WalletTransaction transaction = getWithdrawTransaction(transactionId);

        if (transaction.getWithdrawalStatus() != WalletWithdrawalStatus.PROCESSING) {
            throw new BadRequestException("Yêu cầu rút tiền này không còn ở trạng thái đang xử lý");
        }

        transaction.setWithdrawalStatus(WalletWithdrawalStatus.COMPLETED);
        transaction.setAdminNote(normalizeNullableText(request != null ? request.getNote() : null));
        markProcessed(transaction, admin);

        return mapAdminWithdrawRequest(walletTransactionRepository.save(transaction));
    }

    @Override
    @Transactional
    public AdminWalletWithdrawRequestResponse rejectWithdrawRequest(Long adminUserId, Long transactionId, WalletWithdrawalReviewRequest request) {
        User admin = getAdminActor(adminUserId);
        WalletTransaction transaction = getWithdrawTransaction(transactionId);

        if (transaction.getWithdrawalStatus() != WalletWithdrawalStatus.PROCESSING) {
            throw new BadRequestException("Yêu cầu rút tiền này không còn ở trạng thái đang xử lý");
        }

        String adminNote = normalizeNullableText(request != null ? request.getNote() : null);
        if (!StringUtils.hasText(adminNote)) {
            throw new BadRequestException("Vui lòng nhập lý do từ chối yêu cầu rút tiền");
        }

        Wallet wallet = transaction.getWallet();
        WalletTransaction refundTransaction = applyTransaction(
                wallet,
                Math.abs(transaction.getAmount()),
                WalletTransactionType.REFUND,
                "Hoàn tiền do yêu cầu rút tiền bị từ chối #" + transaction.getId(),
                "WITHDRAWAL_REJECT",
                String.valueOf(transaction.getId())
        );
        copyBankSnapshot(refundTransaction, transaction);
        refundTransaction.setAdminNote(adminNote);
        refundTransaction.setProcessedByUserId(admin.getId());
        refundTransaction.setProcessedByName(admin.getFullName());
        refundTransaction.setProcessedAt(LocalDateTime.now());
        walletTransactionRepository.save(refundTransaction);

        transaction.setWithdrawalStatus(WalletWithdrawalStatus.REJECTED);
        transaction.setAdminNote(adminNote);
        markProcessed(transaction, admin);

        walletRepository.save(wallet);
        return mapAdminWithdrawRequest(walletTransactionRepository.save(transaction));
    }

    @Override
    @Transactional
    public WalletPaymentResult processSubscriptionPurchase(
            User user,
            Long amount,
            String description,
            String referenceId,
            SubscriptionPlanCode planCode,
            SubscriptionBillingCycle billingCycle
    ) {
        if (user.getRole() != UserRole.COACHES && user.getRole() != UserRole.TRAINEES) {
            throw new BadRequestException("Chỉ Coach và Trainees mới có thể mua gói bằng ví");
        }

        Wallet buyerWallet = getOrCreateWallet(user);
        Wallet adminWallet = getOrCreateWallet(getAdminUser());

        if (amount == null || amount <= 0) {
            WalletTransaction buyerTransaction = applyTransaction(
                    buyerWallet,
                    0L,
                    WalletTransactionType.SUBSCRIPTION_PURCHASE,
                    description,
                    "SUBSCRIPTION",
                    referenceId
            );
            buyerTransaction.setSubscriptionPlanCode(planCode);
            buyerTransaction.setSubscriptionBillingCycle(billingCycle);
            walletTransactionRepository.save(buyerTransaction);
            walletRepository.save(buyerWallet);

            return WalletPaymentResult.builder()
                    .chargedAmount(0L)
                    .walletBalanceAfter(buyerWallet.getBalance())
                    .adminWalletBalanceAfter(adminWallet.getBalance())
                    .build();
        }

        WalletTransaction buyerTransaction = applyTransaction(
                buyerWallet,
                -amount,
                WalletTransactionType.SUBSCRIPTION_PURCHASE,
                description,
                "SUBSCRIPTION",
                referenceId
        );
        buyerTransaction.setSubscriptionPlanCode(planCode);
        buyerTransaction.setSubscriptionBillingCycle(billingCycle);
        walletTransactionRepository.save(buyerTransaction);

        applyTransaction(
                adminWallet,
                amount,
                WalletTransactionType.SUBSCRIPTION_REVENUE,
                "Nhận tiền mua gói từ " + user.getFullName(),
                "SUBSCRIPTION",
                referenceId
        );

        walletRepository.save(buyerWallet);
        walletRepository.save(adminWallet);

        return WalletPaymentResult.builder()
                .chargedAmount(amount)
                .walletBalanceAfter(buyerWallet.getBalance())
                .adminWalletBalanceAfter(adminWallet.getBalance())
                .build();
    }

    @Override
    @Transactional
    public BookingSettlementResult settleBookingPayment(Booking booking) {
        if (booking == null) {
            throw new BadRequestException("Booking is required");
        }
        if (Boolean.TRUE.equals(booking.getPaymentSettled())) {
            return BookingSettlementResult.builder()
                    .chargedAmount(booking.getSettledAmount())
                    .adminCommissionAmount(booking.getAdminCommissionAmount())
                    .coachPayoutAmount(booking.getCoachPayoutAmount())
                    .build();
        }
        if (booking.getTrainee() == null || booking.getCoach() == null || booking.getCoach().getUser() == null) {
            throw new BadRequestException("Booking chưa đủ thông tin để quyết toán");
        }

        long totalAmount = normalizeAmount(booking.getPrice());
        if (totalAmount <= 0) {
            throw new BadRequestException("Booking price phải lớn hơn 0 để quyết toán");
        }

        int commissionRate = resolveCommissionRate(booking.getCoach().getUser());
        long adminCommission = Math.round(totalAmount * commissionRate / 100.0d);
        long coachPayout = totalAmount - adminCommission;

        Wallet traineeWallet = getOrCreateWallet(booking.getTrainee());
        Wallet coachWallet = getOrCreateWallet(booking.getCoach().getUser());
        Wallet adminWallet = getOrCreateWallet(getAdminUser());

        String referenceId = String.valueOf(booking.getId());

        applyTransaction(
                traineeWallet,
                -totalAmount,
                WalletTransactionType.BOOKING_PAYMENT,
                "Thanh toán buổi học với coach " + booking.getCoach().getUser().getFullName(),
                "BOOKING",
                referenceId
        );
        applyTransaction(
                adminWallet,
                adminCommission,
                WalletTransactionType.BOOKING_COMMISSION,
                "Nhận hoa hồng booking #" + booking.getId(),
                "BOOKING",
                referenceId
        );
        applyTransaction(
                coachWallet,
                coachPayout,
                WalletTransactionType.BOOKING_COACH_PAYOUT,
                "Nhận tiền buổi học booking #" + booking.getId(),
                "BOOKING",
                referenceId
        );

        walletRepository.save(traineeWallet);
        walletRepository.save(adminWallet);
        walletRepository.save(coachWallet);

        return BookingSettlementResult.builder()
                .chargedAmount(totalAmount)
                .adminCommissionAmount(adminCommission)
                .coachPayoutAmount(coachPayout)
                .traineeWalletBalanceAfter(traineeWallet.getBalance())
                .coachWalletBalanceAfter(coachWallet.getBalance())
                .adminWalletBalanceAfter(adminWallet.getBalance())
                .build();
    }

    private void syncTopUpOrderFromPayOS(WalletTopUpOrder order, PayOSGatewayService.PaymentRequestStatusData statusData) {
        order.setPaymentLinkId(firstNonBlank(order.getPaymentLinkId(), statusData.paymentLinkId()));
        WalletTopUpOrderStatus resolvedStatus = resolveTopUpStatus(statusData.status());

        if (resolvedStatus == WalletTopUpOrderStatus.PAID ||
                ("PAID".equalsIgnoreCase(statusData.status()) && statusData.amountPaid() >= order.getAmount())) {
            creditWalletFromTopUpOrder(order);
            return;
        }

        order.setStatus(resolvedStatus);
        walletTopUpOrderRepository.save(order);
    }

    private void creditWalletFromTopUpOrder(WalletTopUpOrder order) {
        if (order.getStatus() == WalletTopUpOrderStatus.PAID) {
            return;
        }

        applyTransaction(
                order.getWallet(),
                order.getAmount(),
                WalletTransactionType.TOP_UP,
                "Nạp tiền vào ví qua PayOS",
                "TOP_UP",
                String.valueOf(order.getOrderCode())
        );

        order.setStatus(WalletTopUpOrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());

        walletRepository.save(order.getWallet());
        walletTopUpOrderRepository.save(order);
    }

    private int resolveCommissionRate(User coachUser) {
        PlatformSettings settings = platformSettingsRepository.findById(PLATFORM_SETTINGS_ID)
                .orElseGet(this::createDefaultSettings);

        SubscriptionPlanCode planCode = userSubscriptionRepository.findByUserId(coachUser.getId())
                .map(UserSubscription::getPlanCode)
                .orElse(SubscriptionPlanCode.FREE);

        return switch (planCode) {
            case FREE -> settings.getStarterCommissionRate();
            case PRO -> settings.getProCoachCommissionRate();
            case PREMIUM -> settings.getEliteCoachCommissionRate();
        };
    }

    private PlatformSettings createDefaultSettings() {
        return platformSettingsRepository.save(
                PlatformSettings.builder()
                        .id(PLATFORM_SETTINGS_ID)
                        .starterCommissionRate(20)
                        .proCoachCommissionRate(12)
                        .eliteCoachCommissionRate(0)
                        .traineeFreePrice(0L)
                        .traineeProPrice(199000L)
                        .traineePremiumPrice(499000L)
                        .coachStarterPrice(0L)
                        .coachProPrice(499000L)
                        .coachElitePrice(1490000L)
                        .build()
        );
    }

    private WalletTransaction applyTransaction(
            Wallet wallet,
            Long signedAmount,
            WalletTransactionType type,
            String description,
            String referenceType,
            String referenceId
    ) {
        return applyTransaction(wallet, signedAmount, type, description, referenceType, referenceId, null);
    }

    private WalletTransaction applyTransaction(
            Wallet wallet,
            Long signedAmount,
            WalletTransactionType type,
            String description,
            String referenceType,
            String referenceId,
            UserBankAccount bankAccount
    ) {
        long before = wallet.getBalance() == null ? 0L : wallet.getBalance();
        long after = before + signedAmount;

        if (after < 0) {
            throw new BadRequestException("Số dư ví không đủ để thực hiện giao dịch");
        }

        wallet.setBalance(after);
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .type(type)
                .amount(signedAmount)
                .balanceBefore(before)
                .balanceAfter(after)
                .description(description)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .bankCode(bankAccount != null ? bankAccount.getBankCode() : null)
                .bankName(bankAccount != null ? bankAccount.getBankName() : null)
                .bankAccountNumber(bankAccount != null ? bankAccount.getAccountNumber() : null)
                .bankAccountHolderName(bankAccount != null ? bankAccount.getAccountHolderName() : null)
                .bankBranch(bankAccount != null ? bankAccount.getBranch() : null)
                .build();
        return walletTransactionRepository.save(transaction);
    }

    private WalletTransaction getWithdrawTransaction(Long transactionId) {
        WalletTransaction transaction = walletTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("WalletTransaction", "id", transactionId));

        if (transaction.getType() != WalletTransactionType.WITHDRAWAL) {
            throw new BadRequestException("Giao dịch này không phải yêu cầu rút tiền");
        }
        if (transaction.getWithdrawalStatus() == null) {
            throw new BadRequestException("Giao dịch rút tiền này không hỗ trợ quy trình duyệt");
        }
        return transaction;
    }

    private User getAdminActor(Long adminUserId) {
        User admin = getUser(adminUserId);
        if (admin.getRole() != UserRole.ADMIN) {
            throw new BadRequestException("Chỉ Admin mới có quyền xử lý yêu cầu rút tiền");
        }
        return admin;
    }

    private void markProcessed(WalletTransaction transaction, User admin) {
        transaction.setProcessedByUserId(admin.getId());
        transaction.setProcessedByName(admin.getFullName());
        transaction.setProcessedAt(LocalDateTime.now());
    }

    private void copyBankSnapshot(WalletTransaction target, WalletTransaction source) {
        target.setBankCode(source.getBankCode());
        target.setBankName(source.getBankName());
        target.setBankAccountNumber(source.getBankAccountNumber());
        target.setBankAccountHolderName(source.getBankAccountHolderName());
        target.setBankBranch(source.getBankBranch());
    }

    private Wallet getOrCreateWallet(User user) {
        return walletRepository.findByUserId(user.getId())
                .orElseGet(() -> walletRepository.save(
                        Wallet.builder()
                                .user(user)
                                .balance(0L)
                                .currency("VND")
                                .build()
                ));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private User getAdminUser() {
        return userRepository.findFirstByRole(UserRole.ADMIN)
                .orElseThrow(() -> new BadRequestException("Chưa có tài khoản Admin để nhận tiền hệ thống"));
    }

    private long normalizeAmount(Double value) {
        if (value == null) {
            return 0L;
        }
        return Math.round(value);
    }

    private WalletResponse mapWallet(Wallet wallet) {
        return WalletResponse.builder()
                .walletId(wallet.getId())
                .userId(wallet.getUser().getId())
                .ownerName(wallet.getUser().getFullName())
                .role(wallet.getUser().getRole())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    private WalletTransactionResponse mapTransaction(WalletTransaction transaction) {
        return WalletTransactionResponse.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .referenceType(transaction.getReferenceType())
                .referenceId(transaction.getReferenceId())
                .subscriptionPlanCode(transaction.getSubscriptionPlanCode())
                .subscriptionBillingCycle(transaction.getSubscriptionBillingCycle())
                .bankCode(transaction.getBankCode())
                .bankName(transaction.getBankName())
                .bankAccountNumber(transaction.getBankAccountNumber())
                .bankAccountHolderName(transaction.getBankAccountHolderName())
                .bankBranch(transaction.getBankBranch())
                .withdrawalStatus(transaction.getWithdrawalStatus())
                .adminNote(transaction.getAdminNote())
                .processedByUserId(transaction.getProcessedByUserId())
                .processedByName(transaction.getProcessedByName())
                .processedAt(transaction.getProcessedAt())
                .createdAt(transaction.getCreatedAt())
                .build();
    }


    private WalletWithdrawResponse mapWithdrawResponse(WalletTransaction transaction) {
        Wallet wallet = transaction.getWallet();
        User owner = wallet.getUser();
        return WalletWithdrawResponse.builder()
                .transactionId(transaction.getId())
                .userId(owner.getId())
                .ownerName(owner.getFullName())
                .role(owner.getRole())
                .type(transaction.getType())
                .amount(Math.abs(transaction.getAmount()))
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .currency(wallet.getCurrency())
                .description(transaction.getDescription())
                .referenceType(transaction.getReferenceType())
                .referenceId(transaction.getReferenceId())
                .subscriptionPlanCode(transaction.getSubscriptionPlanCode())
                .subscriptionBillingCycle(transaction.getSubscriptionBillingCycle())
                .bankCode(transaction.getBankCode())
                .bankName(transaction.getBankName())
                .bankAccountNumber(transaction.getBankAccountNumber())
                .bankAccountHolderName(transaction.getBankAccountHolderName())
                .bankBranch(transaction.getBankBranch())
                .withdrawalStatus(transaction.getWithdrawalStatus())
                .adminNote(transaction.getAdminNote())
                .processedByUserId(transaction.getProcessedByUserId())
                .processedByName(transaction.getProcessedByName())
                .processedAt(transaction.getProcessedAt())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    private AdminWalletWithdrawRequestResponse mapAdminWithdrawRequest(WalletTransaction transaction) {
        Wallet wallet = transaction.getWallet();
        User owner = wallet.getUser();
        return AdminWalletWithdrawRequestResponse.builder()
                .transactionId(transaction.getId())
                .walletId(wallet.getId())
                .userId(owner.getId())
                .ownerName(owner.getFullName())
                .role(owner.getRole())
                .type(transaction.getType())
                .withdrawalStatus(transaction.getWithdrawalStatus())
                .amount(Math.abs(transaction.getAmount()))
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .currency(wallet.getCurrency())
                .description(transaction.getDescription())
                .referenceType(transaction.getReferenceType())
                .referenceId(transaction.getReferenceId())
                .subscriptionPlanCode(transaction.getSubscriptionPlanCode())
                .subscriptionBillingCycle(transaction.getSubscriptionBillingCycle())
                .bankCode(transaction.getBankCode())
                .bankName(transaction.getBankName())
                .bankAccountNumber(transaction.getBankAccountNumber())
                .bankAccountHolderName(transaction.getBankAccountHolderName())
                .bankBranch(transaction.getBankBranch())
                .adminNote(transaction.getAdminNote())
                .processedByUserId(transaction.getProcessedByUserId())
                .processedByName(transaction.getProcessedByName())
                .processedAt(transaction.getProcessedAt())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    private WalletBankAccountResponse mapBankAccount(UserBankAccount bankAccount) {
        return WalletBankAccountResponse.builder()
                .id(bankAccount.getId())
                .userId(bankAccount.getUser().getId())
                .bankCode(bankAccount.getBankCode())
                .bankName(bankAccount.getBankName())
                .accountNumber(bankAccount.getAccountNumber())
                .accountHolderName(bankAccount.getAccountHolderName())
                .branch(bankAccount.getBranch())
                .createdAt(bankAccount.getCreatedAt())
                .updatedAt(bankAccount.getUpdatedAt())
                .build();
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String normalizeNullableText(String value) {
        return normalizeText(value);
    }

    private String normalizeDigits(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.replaceAll("\\D", "");
    }

    private WalletTopUpResponse mapTopUp(WalletTopUpOrder order, Long walletBalance) {
        return WalletTopUpResponse.builder()
                .orderCode(order.getOrderCode())
                .amount(order.getAmount())
                .description(order.getDescription())
                .status(order.getStatus())
                .paymentLinkId(order.getPaymentLinkId())
                .checkoutUrl(order.getCheckoutUrl())
                .qrCode(order.getQrCode())
                .currency(order.getWallet().getCurrency())
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaidAt())
                .walletBalance(walletBalance)
                .build();
    }

    private WalletTopUpOrderStatus resolveTopUpStatus(String payosStatus) {
        if (!StringUtils.hasText(payosStatus)) {
            return WalletTopUpOrderStatus.PENDING;
        }
        return switch (payosStatus.trim().toUpperCase()) {
            case "PAID" -> WalletTopUpOrderStatus.PAID;
            case "CANCELLED" -> WalletTopUpOrderStatus.CANCELLED;
            case "EXPIRED" -> WalletTopUpOrderStatus.EXPIRED;
            case "FAILED" -> WalletTopUpOrderStatus.FAILED;
            default -> WalletTopUpOrderStatus.PENDING;
        };
    }

    private Long generateOrderCode() {
        long orderCode;
        do {
            orderCode = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(10, 999);
        } while (walletTopUpOrderRepository.existsByOrderCode(orderCode));
        return orderCode;
    }

    private String buildTopUpDescription(Long orderCode) {
        String suffix = String.valueOf(orderCode);
        if (suffix.length() > 10) {
            suffix = suffix.substring(suffix.length() - 10);
        }
        return "NAPVI" + suffix;
    }

    private String writeJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String firstNonBlank(String current, String fallback) {
        return StringUtils.hasText(current) ? current : fallback;
    }
}
