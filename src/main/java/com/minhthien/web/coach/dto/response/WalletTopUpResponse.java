package com.minhthien.web.coach.dto.response;

import com.minhthien.web.coach.enums.WalletTopUpOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTopUpResponse {
    private Long orderCode;
    private Long amount;
    private String description;
    private WalletTopUpOrderStatus status;
    private String paymentLinkId;
    private String checkoutUrl;
    private String qrCode;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private Long walletBalance;
}
