package com.minhthien.web.coach.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendConversationMessageRequest {

    @NotBlank(message = "content is required")
    private String content;
}
