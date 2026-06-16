package com.minhthien.web.coach.dto.response;

import com.minhthien.web.coach.enums.ChatMessageType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageResponse {

    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderUsername;
    private String senderFullName;
    private Long receiverId;
    private String receiverUsername;
    private String receiverFullName;
    private String content;
    private ChatMessageType messageType;
    private String attachmentUrl;
    private String attachmentPublicId;
    private String attachmentFileName;
    private String attachmentMimeType;
    private Long attachmentSizeBytes;
    private Boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private Boolean ownMessage;
}
