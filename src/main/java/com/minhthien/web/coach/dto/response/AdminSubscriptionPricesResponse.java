package com.minhthien.web.coach.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSubscriptionPricesResponse {

    private TraineePrices trainee;
    private CoachPrices coach;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TraineePrices {
        private Long free;
        private Long pro;
        private Long premium;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoachPrices {
        private Long starter;
        private Long proCoach;
        private Long eliteCoach;
    }
}
