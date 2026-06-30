package com.minhthien.web.coach.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSettlementResult {
    private Long chargedAmount;
    private Long adminCommissionAmount;
    private Long coachPayoutAmount;
    private Long traineeWalletBalanceAfter;
    private Long coachWalletBalanceAfter;
    private Long adminWalletBalanceAfter;
    private Long payoutRecipientUserId;
    private com.minhthien.web.coach.enums.UserRole payoutRecipientRole;
    private String payoutRecipientName;
    private Long gymId;
}
