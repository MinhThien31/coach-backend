package com.minhthien.web.coach.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletWithdrawalReviewRequest {

    @Size(max = 255, message = "Note must not exceed 255 characters")
    private String note;
}
