package com.minhthien.web.coach.dto.request;

import lombok.Data;

@Data
public class CreateConversationRequest {

    private Long participantId;

    private Long coachProfileId;
}
