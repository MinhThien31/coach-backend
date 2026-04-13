package com.minhthien.web.coach.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendChatMessageRequest {

    @NotNull(message = "conversationId is required")
    private Long conversationId;

    @NotBlank(message = "content is required")
    private String content;
}
