package com.minhthien.web.coach.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConversationResponse {

    private Long id;
    private Long participantId;
    private String participantUsername;
    private String participantFullName;
    private String participantAvatarUrl;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private LocalDateTime updatedAt;
}
