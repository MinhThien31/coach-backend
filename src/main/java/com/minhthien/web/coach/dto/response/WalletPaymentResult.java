package com.minhthien.web.coach.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletPaymentResult {
    private Long chargedAmount;
    private Long walletBalanceAfter;
    private Long adminWalletBalanceAfter;
}
