package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.response.ChatMessageResponse;
import com.minhthien.web.coach.dto.response.ConversationResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ChatService {

    ConversationResponse createOrGetConversation(Long currentUserId, Long participantId);

    List<ConversationResponse> getMyConversations(Long currentUserId);

    Page<ChatMessageResponse> getConversationMessages(Long currentUserId, Long conversationId, int page, int size);

    ChatMessageResponse sendMessage(Long currentUserId, Long conversationId, String content);
}
