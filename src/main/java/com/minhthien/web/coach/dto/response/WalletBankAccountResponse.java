package com.minhthien.web.coach.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletBankAccountResponse {
    private Long id;
    private Long userId;
    private String bankCode;
    private String bankName;
    private String accountNumber;
    private String accountHolderName;
    private String branch;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
