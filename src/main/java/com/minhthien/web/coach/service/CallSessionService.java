package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.request.VideoCallSignalRequest;
import com.minhthien.web.coach.dto.response.CallSessionResponse;
import com.minhthien.web.coach.entity.User;
import org.springframework.data.domain.Page;

public interface CallSessionService {

    VideoCallSignalRequest handleSignal(User currentUser, VideoCallSignalRequest request);

    Page<CallSessionResponse> getConversationCalls(Long currentUserId, Long conversationId, int page, int size);
}
