package com.minhthien.web.coach.dto.response;

import com.minhthien.web.coach.enums.SubscriptionBillingCycle;
import com.minhthien.web.coach.enums.SubscriptionPlanCode;
import com.minhthien.web.coach.enums.WalletTransactionType;
import com.minhthien.web.coach.enums.WalletWithdrawalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionResponse {
    private Long id;
    private WalletTransactionType type;
    private Long amount;
    private Long balanceBefore;
    private Long balanceAfter;
    private String description;
    private String referenceType;
    private String referenceId;
    private SubscriptionPlanCode subscriptionPlanCode;
    private SubscriptionBillingCycle subscriptionBillingCycle;
    private String bankCode;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountHolderName;
    private String bankBranch;
    private WalletWithdrawalStatus withdrawalStatus;
    private String adminNote;
    private Long processedByUserId;
    private String processedByName;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
}
