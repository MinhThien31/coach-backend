package com.minhthien.web.coach.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCommissionSettingsRequest {

    @NotNull(message = "Starter commission rate is required")
    @Min(value = 0, message = "Starter commission rate must be at least 0")
    @Max(value = 100, message = "Starter commission rate must be at most 100")
    private Integer starter;

    @NotNull(message = "Pro Coach commission rate is required")
    @Min(value = 0, message = "Pro Coach commission rate must be at least 0")
    @Max(value = 100, message = "Pro Coach commission rate must be at most 100")
    private Integer proCoach;

    @NotNull(message = "Elite Coach commission rate is required")
    @Min(value = 0, message = "Elite Coach commission rate must be at least 0")
    @Max(value = 100, message = "Elite Coach commission rate must be at most 100")
    private Integer eliteCoach;
}
