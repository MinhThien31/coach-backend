package com.minhthien.web.coach.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletWithdrawRequest {

    @NotNull(message = "Withdraw amount is required")
    @Min(value = 1000, message = "Withdraw amount must be at least 1000")
    private Long amount;

    @Size(max = 255, message = "Note must not exceed 255 characters")
    private String note;
}
