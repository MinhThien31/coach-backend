package com.minhthien.web.coach.dto.response;

import com.minhthien.web.coach.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPurchaseHistoryResponse {
    private Long userId;
    private String ownerName;
    private UserRole role;
    private Integer totalPurchases;
    private CurrentSubscriptionResponse currentSubscription;
    private List<SubscriptionPurchaseHistoryItemResponse> purchases;
}
