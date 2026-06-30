package com.minhthien.web.coach.dto.request;

import com.minhthien.web.coach.enums.GymProfileStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class GymOwnerRequests {

    @Data
    public static class GymProfileUpdateRequest {
        @Size(max = 150)
        private String name;

        @Size(max = 255)
        private String address;

        @Size(max = 30)
        private String hotline;

        @Size(max = 2000)
        private String description;

        @Size(max = 500)
        private String logoUrl;

        @Size(max = 500)
        private String coverUrl;
    }

    @Data
    public static class GymCoachAddRequest {
        private Long coachProfileId;

        @Size(max = 150)
        private String emailOrUsername;
    }

    @Data
    public static class AdminGymStatusUpdateRequest {
        @NotNull
        private GymProfileStatus status;
    }
}
