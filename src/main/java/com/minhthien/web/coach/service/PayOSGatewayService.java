package com.minhthien.web.coach.service;

import java.util.Map;

public interface PayOSGatewayService {

    PaymentLinkData createPaymentLink(Long orderCode, Long amount, String description, String buyerName, String buyerEmail, String buyerPhone);

    PaymentRequestStatusData getPaymentRequestStatus(Long orderCode);

    VerifiedWebhookData verifyWebhook(Map<String, Object> webhookPayload);

    record PaymentLinkData(
            Long orderCode,
            Long amount,
            String paymentLinkId,
            String checkoutUrl,
            String qrCode,
            String status
    ) {}

    record PaymentRequestStatusData(
            Long orderCode,
            Long amount,
            Long amountPaid,
            String status,
            String paymentLinkId
    ) {}

    record VerifiedWebhookData(
            Long orderCode,
            Long amount,
            String reference,
            String paymentLinkId,
            String code,
            String desc,
            boolean success,
            Map<String, Object> rawPayload
    ) {}
}
