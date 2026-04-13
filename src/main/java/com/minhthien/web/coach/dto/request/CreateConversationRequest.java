package com.minhthien.web.coach.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateConversationRequest {

    @NotNull(message = "participantId is required")
    private Long participantId;
}
