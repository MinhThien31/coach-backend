package com.minhthien.web.coach.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTopUpRequest {

    @NotNull(message = "Top up amount is required")
    @Min(value = 1000, message = "Top up amount must be at least 1000")
    private Long amount;
}
