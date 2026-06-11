package com.minhthien.web.coach.dto.response;

import com.minhthien.web.coach.enums.UserRole;
import com.minhthien.web.coach.enums.WalletTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminWalletHistoryItemResponse {
    private String id;
    private String source;
    private Long userId;
    private String username;
    private String ownerName;
    private String email;
    private UserRole role;
    private WalletTransactionType type;
    private Long amount;
    private Long balanceBefore;
    private Long balanceAfter;
    private String currency;
    private String status;
    private String description;
    private String referenceType;
    private String referenceId;
    private String bankCode;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountHolderName;
    private String bankBranch;
    private String adminNote;
    private Long processedByUserId;
    private String processedByName;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
}
