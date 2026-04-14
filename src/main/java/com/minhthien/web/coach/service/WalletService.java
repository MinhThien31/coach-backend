package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.request.WalletTopUpRequest;
import com.minhthien.web.coach.dto.response.AdminWalletOverviewResponse;
import com.minhthien.web.coach.dto.response.BookingSettlementResult;
import com.minhthien.web.coach.dto.response.WalletPaymentResult;
import com.minhthien.web.coach.dto.response.WalletResponse;
import com.minhthien.web.coach.dto.response.WalletTopUpResponse;
import com.minhthien.web.coach.dto.response.WalletTransactionResponse;
import com.minhthien.web.coach.entity.Booking;
import com.minhthien.web.coach.entity.User;

import java.util.List;
import java.util.Map;

public interface WalletService {

    WalletResponse getMyWallet(Long currentUserId);

    WalletTopUpResponse createTopUpPayment(Long currentUserId, WalletTopUpRequest request);

    WalletTopUpResponse getTopUpStatus(Long currentUserId, Long orderCode);

    void handlePayOSWebhook(Map<String, Object> payload);

    List<WalletTransactionResponse> getMyTransactions(Long currentUserId);

    AdminWalletOverviewResponse getAdminWalletOverview();

    WalletPaymentResult processSubscriptionPurchase(User user, Long amount, String description, String referenceId);

    BookingSettlementResult settleBookingPayment(Booking booking);
}
