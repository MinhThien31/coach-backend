package com.minhthien.web.coach.dto.response;

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
public class WalletTransactionResponse {
    private Long id;
    private WalletTransactionType type;
    private Long amount;
    private Long balanceBefore;
    private Long balanceAfter;
    private String description;
    private String referenceType;
    private String referenceId;
    private LocalDateTime createdAt;
}
