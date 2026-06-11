package com.minhthien.web.coach.dto.response;

import com.minhthien.web.coach.enums.CallSessionStatus;
import com.minhthien.web.coach.enums.CallType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CallSessionResponse {

    private Long id;
    private Long conversationId;
    private Long callerId;
    private String callerUsername;
    private String callerFullName;
    private Long calleeId;
    private String calleeUsername;
    private String calleeFullName;
    private CallType callType;
    private CallSessionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime endedAt;
    private Long durationSeconds;
    private LocalDateTime createdAt;
    private Boolean ownCall;
}
