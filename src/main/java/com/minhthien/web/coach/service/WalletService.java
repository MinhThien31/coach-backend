package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.request.WalletBankAccountUpsertRequest;
import com.minhthien.web.coach.dto.request.WalletTopUpRequest;
import com.minhthien.web.coach.dto.request.WalletWithdrawRequest;
import com.minhthien.web.coach.dto.request.WalletWithdrawalReviewRequest;
import com.minhthien.web.coach.dto.response.AdminWalletOverviewResponse;
import com.minhthien.web.coach.dto.response.AdminWalletWithdrawRequestResponse;
import com.minhthien.web.coach.dto.response.BookingSettlementResult;
import com.minhthien.web.coach.dto.response.WalletBankAccountResponse;
import com.minhthien.web.coach.dto.response.WalletPaymentResult;
import com.minhthien.web.coach.dto.response.WalletResponse;
import com.minhthien.web.coach.dto.response.WalletTopUpResponse;
import com.minhthien.web.coach.dto.response.WalletWithdrawResponse;
import com.minhthien.web.coach.dto.response.WalletTransactionResponse;
import com.minhthien.web.coach.entity.Booking;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.enums.SubscriptionBillingCycle;
import com.minhthien.web.coach.enums.SubscriptionPlanCode;
import com.minhthien.web.coach.enums.WalletWithdrawalStatus;

import java.util.List;
import java.util.Map;

public interface WalletService {

    WalletResponse getMyWallet(Long currentUserId);

    WalletTopUpResponse createTopUpPayment(Long currentUserId, WalletTopUpRequest request);

    WalletTopUpResponse getTopUpStatus(Long currentUserId, Long orderCode);

    WalletBankAccountResponse upsertMyBankAccount(Long currentUserId, WalletBankAccountUpsertRequest request);

    WalletBankAccountResponse getMyBankAccount(Long currentUserId);

    WalletWithdrawResponse withdrawFromMyWallet(Long currentUserId, WalletWithdrawRequest request);

    void handlePayOSWebhook(Map<String, Object> payload);

    List<WalletTransactionResponse> getMyTransactions(Long currentUserId);

    AdminWalletOverviewResponse getAdminWalletOverview();

    List<AdminWalletWithdrawRequestResponse> getAdminWithdrawRequests(WalletWithdrawalStatus status);

    AdminWalletWithdrawRequestResponse approveWithdrawRequest(Long adminUserId, Long transactionId, WalletWithdrawalReviewRequest request);

    AdminWalletWithdrawRequestResponse rejectWithdrawRequest(Long adminUserId, Long transactionId, WalletWithdrawalReviewRequest request);

    WalletPaymentResult processSubscriptionPurchase(User user, Long amount, String description, String referenceId,
                                                    SubscriptionPlanCode planCode, SubscriptionBillingCycle billingCycle);

    BookingSettlementResult settleBookingPayment(Booking booking);
}
