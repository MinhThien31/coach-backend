package com.minhthien.web.coach.service.impl;

import com.minhthien.web.coach.dto.response.ChatMessageResponse;
import com.minhthien.web.coach.dto.response.ConversationResponse;
import com.minhthien.web.coach.entity.ChatMessage;
import com.minhthien.web.coach.entity.Conversation;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.exception.BadRequestException;
import com.minhthien.web.coach.exception.ResourceNotFoundException;
import com.minhthien.web.coach.exception.UnauthorizedException;
import com.minhthien.web.coach.repository.ChatMessageRepository;
import com.minhthien.web.coach.repository.ConversationRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public ConversationResponse createOrGetConversation(Long currentUserId, Long participantId) {
        if (currentUserId.equals(participantId)) {
            throw new BadRequestException("You cannot create a conversation with yourself");
        }

        User currentUser = getUser(currentUserId);
        User participant = getUser(participantId);

        Long firstId = Math.min(currentUserId, participantId);
        Long secondId = Math.max(currentUserId, participantId);

        Conversation conversation = conversationRepository
                .findByUserOneIdAndUserTwoId(firstId, secondId)
                .orElseGet(() -> conversationRepository.save(
                        Conversation.builder()
                                .userOne(firstId.equals(currentUser.getId()) ? currentUser : participant)
                                .userTwo(secondId.equals(currentUser.getId()) ? currentUser : participant)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build()
                ));

        return mapConversation(conversation, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getMyConversations(Long currentUserId) {
        return conversationRepository.findAllByParticipantIdOrderByUpdatedAtDesc(currentUserId)
                .stream()
                .map(conversation -> mapConversation(conversation, currentUserId))
                .toList();
    }

    @Override
    @Transactional
    public Page<ChatMessageResponse> getConversationMessages(Long currentUserId, Long conversationId, int page, int size) {
        Conversation conversation = getConversationForUser(currentUserId, conversationId);

        Page<ChatMessage> messagePage = chatMessageRepository.findByConversationIdOrderByCreatedAtDesc(
                conversationId,
                PageRequest.of(page, size)
        );

        List<ChatMessage> managedMessages = messagePage.getContent();
        managedMessages.stream()
                .filter(message -> Boolean.FALSE.equals(message.getRead()) && message.getReceiver().getId().equals(currentUserId))
                .forEach(message -> {
                    message.setRead(true);
                    message.setReadAt(LocalDateTime.now());
                });

        List<ChatMessageResponse> messages = managedMessages.stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .map(message -> mapMessage(message, currentUserId))
                .toList();

        return new PageImpl<>(messages, messagePage.getPageable(), messagePage.getTotalElements());
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(Long currentUserId, Long conversationId, String content) {
        Conversation conversation = getConversationForUser(currentUserId, conversationId);
        User sender = getUser(currentUserId);
        User receiver = getOtherParticipant(conversation, currentUserId);

        String normalizedContent = content == null ? null : content.trim();
        if (normalizedContent == null || normalizedContent.isEmpty()) {
            throw new BadRequestException("Message content must not be blank");
        }

        ChatMessage message = ChatMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .receiver(receiver)
                .content(normalizedContent)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return mapMessage(savedMessage, currentUserId);
    }

    private Conversation getConversationForUser(Long currentUserId, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

        boolean isParticipant = conversation.getUserOne().getId().equals(currentUserId)
                || conversation.getUserTwo().getId().equals(currentUserId);

        if (!isParticipant) {
            throw new UnauthorizedException("You are not allowed to access this conversation");
        }
        return conversation;
    }

    private User getOtherParticipant(Conversation conversation, Long currentUserId) {
        if (conversation.getUserOne().getId().equals(currentUserId)) {
            return conversation.getUserTwo();
        }
        return conversation.getUserOne();
    }

    private ConversationResponse mapConversation(Conversation conversation, Long currentUserId) {
        User participant = getOtherParticipant(conversation, currentUserId);
        ChatMessage latestMessage = chatMessageRepository.findTopByConversationIdOrderByCreatedAtDesc(conversation.getId())
                .orElse(null);

        return ConversationResponse.builder()
                .id(conversation.getId())
                .participantId(participant.getId())
                .participantUsername(participant.getUsername())
                .participantFullName(participant.getFullName())
                .participantAvatarUrl(participant.getAvatarUrl())
                .lastMessage(latestMessage != null ? latestMessage.getContent() : null)
                .lastMessageAt(latestMessage != null ? latestMessage.getCreatedAt() : null)
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    private ChatMessageResponse mapMessage(ChatMessage message, Long currentUserId) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .senderFullName(message.getSender().getFullName())
                .receiverId(message.getReceiver().getId())
                .receiverUsername(message.getReceiver().getUsername())
                .receiverFullName(message.getReceiver().getFullName())
                .content(message.getContent())
                .read(message.getRead())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .ownMessage(message.getSender().getId().equals(currentUserId))
                .build();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
}
