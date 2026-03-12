package com.minhthien.web.coach.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCommissionSettingsResponse {
    private Integer starter;
    private Integer proCoach;
    private Integer eliteCoach;
}
