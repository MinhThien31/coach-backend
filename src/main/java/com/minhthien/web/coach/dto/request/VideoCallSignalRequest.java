package com.minhthien.web.coach.dto.request;

import lombok.Data;

@Data
public class VideoCallSignalRequest {
    private Long callId;
    private Long conversationId;
    private String type;
    private String callType;
    private String targetUsername;
    private String senderUsername;
    private String senderFullName;
    private Object payload;
}
