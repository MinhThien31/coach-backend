package com.minhthien.web.coach.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminWalletOverviewResponse {
    private WalletResponse wallet;
    private long totalTransactions;
    private List<WalletTransactionResponse> recentTransactions;
}
