package com.minhthien.web.coach.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSubscriptionPricesRequest {

    @Valid
    @NotNull(message = "Trainee pricing is required")
    private TraineePrices trainee;

    @Valid
    @NotNull(message = "Coach pricing is required")
    private CoachPrices coach;

    @Data
    public static class TraineePrices {

        @NotNull(message = "Free plan price is required")
        @Min(value = 0, message = "Free plan price must be at least 0")
        private Long free;

        @NotNull(message = "Pro plan price is required")
        @Min(value = 0, message = "Pro plan price must be at least 0")
        private Long pro;

        @NotNull(message = "Premium plan price is required")
        @Min(value = 0, message = "Premium plan price must be at least 0")
        private Long premium;
    }

    @Data
    public static class CoachPrices {

        @NotNull(message = "Starter plan price is required")
        @Min(value = 0, message = "Starter plan price must be at least 0")
        private Long starter;

        @NotNull(message = "Pro Coach plan price is required")
        @Min(value = 0, message = "Pro Coach plan price must be at least 0")
        private Long proCoach;

        @NotNull(message = "Elite Coach plan price is required")
        @Min(value = 0, message = "Elite Coach plan price must be at least 0")
        private Long eliteCoach;
    }
}
