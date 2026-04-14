package com.minhthien.web.coach.dto.response;

import com.minhthien.web.coach.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {
    private Long walletId;
    private Long userId;
    private String ownerName;
    private UserRole role;
    private Long balance;
    private String currency;
    private LocalDateTime updatedAt;
}
