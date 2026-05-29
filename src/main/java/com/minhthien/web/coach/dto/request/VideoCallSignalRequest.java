package com.minhthien.web.coach.dto.request;

import lombok.Data;

@Data
public class VideoCallSignalRequest {
    private String type; // "offer", "answer", "ice", "end", "call"
    private String targetUsername;
    private String senderUsername;
    private Object payload;
}
