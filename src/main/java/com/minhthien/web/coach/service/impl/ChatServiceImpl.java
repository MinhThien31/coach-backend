package com.minhthien.web.coach.service.impl;

import com.minhthien.web.coach.dto.response.ChatMessageResponse;
import com.minhthien.web.coach.dto.response.ConversationResponse;
import com.minhthien.web.coach.entity.ChatMessage;
import com.minhthien.web.coach.entity.Conversation;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.enums.ChatMessageType;
import com.minhthien.web.coach.exception.BadRequestException;
import com.minhthien.web.coach.exception.ResourceNotFoundException;
import com.minhthien.web.coach.exception.UnauthorizedException;
import com.minhthien.web.coach.repository.ChatMessageRepository;
import com.minhthien.web.coach.repository.ConversationRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.service.ChatAttachmentService;
import com.minhthien.web.coach.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatAttachmentService chatAttachmentService;

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

        Conversation conversation = conversationRepository.findByUserOneIdAndUserTwoId(firstId, secondId)
                .orElseGet(() -> conversationRepository.save(
                        Conversation.builder()
                                .userOne(firstId.equals(currentUser.getId()) ? currentUser : participant)
                                .userTwo(secondId.equals(currentUser.getId()) ? currentUser : participant)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build()
                ));

        ChatMessage latestMessage = chatMessageRepository.findTopByConversationIdOrderByCreatedAtDesc(conversation.getId())
                .orElse(null);
        return mapConversation(conversation, currentUserId, latestMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getMyConversations(Long currentUserId) {
        List<Conversation> conversations = conversationRepository.findAllByParticipantIdOrderByUpdatedAtDesc(currentUserId);
        if (conversations.isEmpty()) {
            return List.of();
        }

        List<Long> conversationIds = conversations.stream()
                .map(Conversation::getId)
                .toList();

        Map<Long, ChatMessage> latestMessageByConversationId = chatMessageRepository.findLatestMessagesByConversationIds(conversationIds)
                .stream()
                .collect(Collectors.toMap(message -> message.getConversation().getId(), Function.identity()));

        return conversations.stream()
                .map(conversation -> mapConversation(
                        conversation,
                        currentUserId,
                        latestMessageByConversationId.get(conversation.getId())
                ))
                .toList();
    }

    @Override
    @Transactional
    public Page<ChatMessageResponse> getConversationMessages(Long currentUserId, Long conversationId, int page, int size) {
        getConversationForUser(currentUserId, conversationId);

        Page<ChatMessage> messagePage = chatMessageRepository.findByConversationIdOrderByCreatedAtDesc(
                conversationId,
                PageRequest.of(page, size)
        );

        List<ChatMessage> managedMessages = messagePage.getContent();
        LocalDateTime readAt = LocalDateTime.now();
        managedMessages.stream()
                .filter(message -> Boolean.FALSE.equals(message.getRead()) && message.getReceiver().getId().equals(currentUserId))
                .forEach(message -> {
                    message.setRead(true);
                    message.setReadAt(readAt);
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

        ChatMessage savedMessage = saveMessage(conversation, sender, receiver, normalizedContent, ChatMessageType.TEXT, null);

        return mapMessage(savedMessage, currentUserId);
    }

    @Override
    @Transactional
    public ChatMessageResponse sendAttachment(Long currentUserId, Long conversationId, MultipartFile file, String content) {
        Conversation conversation = getConversationForUser(currentUserId, conversationId);
        User sender = getUser(currentUserId);
        User receiver = getOtherParticipant(conversation, currentUserId);
        ChatAttachmentService.UploadedAttachment attachment = chatAttachmentService.upload(file);
        String normalizedContent = normalizeAttachmentContent(content, attachment.messageType());

        ChatMessage savedMessage = saveMessage(
                conversation,
                sender,
                receiver,
                normalizedContent,
                attachment.messageType(),
                attachment
        );

        return mapMessage(savedMessage, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long currentUserId) {
        return chatMessageRepository.countByReceiverIdAndReadFalse(currentUserId);
    }

    @Override
    @Transactional
    public void markConversationRead(Long currentUserId, Long conversationId) {
        getConversationForUser(currentUserId, conversationId);
        chatMessageRepository.markConversationRead(conversationId, currentUserId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void deleteConversation(Long currentUserId, Long conversationId) {
        Conversation conversation = getConversationForUser(currentUserId, conversationId);
        chatMessageRepository.deleteByConversationId(conversationId);
        conversationRepository.delete(conversation);
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

    private ConversationResponse mapConversation(Conversation conversation, Long currentUserId, ChatMessage latestMessage) {
        User participant = getOtherParticipant(conversation, currentUserId);
        return ConversationResponse.builder()
                .id(conversation.getId())
                .participantId(participant.getId())
                .participantUsername(participant.getUsername())
                .participantFullName(participant.getFullName())
                .participantAvatarUrl(participant.getAvatarUrl())
                .lastMessage(latestMessage != null ? getConversationPreview(latestMessage) : null)
                .lastMessageAt(latestMessage != null ? latestMessage.getCreatedAt() : null)
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    private ChatMessageResponse mapMessage(ChatMessage message, Long currentUserId) {
        ChatMessageType messageType = message.getMessageType() == null ? ChatMessageType.TEXT : message.getMessageType();
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
                .messageType(messageType)
                .attachmentUrl(message.getAttachmentUrl())
                .attachmentPublicId(message.getAttachmentPublicId())
                .attachmentFileName(message.getAttachmentFileName())
                .attachmentMimeType(message.getAttachmentMimeType())
                .attachmentSizeBytes(message.getAttachmentSizeBytes())
                .read(message.getRead())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .ownMessage(message.getSender().getId().equals(currentUserId))
                .build();
    }

    private ChatMessage saveMessage(
            Conversation conversation,
            User sender,
            User receiver,
            String content,
            ChatMessageType messageType,
            ChatAttachmentService.UploadedAttachment attachment
    ) {
        ChatMessage.ChatMessageBuilder builder = ChatMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .messageType(messageType)
                .read(false)
                .createdAt(LocalDateTime.now());

        if (attachment != null) {
            builder
                    .attachmentUrl(attachment.url())
                    .attachmentPublicId(attachment.publicId())
                    .attachmentFileName(attachment.fileName())
                    .attachmentMimeType(attachment.mimeType())
                    .attachmentSizeBytes(attachment.sizeBytes());
        }

        ChatMessage savedMessage = chatMessageRepository.save(builder.build());
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        return savedMessage;
    }

    private String normalizeAttachmentContent(String content, ChatMessageType messageType) {
        String normalizedContent = content == null ? null : content.trim();
        if (normalizedContent != null && !normalizedContent.isEmpty()) {
            return normalizedContent;
        }
        return switch (messageType) {
            case IMAGE -> "Đã gửi một hình ảnh";
            case PDF -> "Đã gửi một PDF";
            case VIDEO -> "Đã gửi một video";
            case FILE -> "Đã gửi một tệp";
            case TEXT -> "Đã gửi một tin nhắn";
        };
    }

    private String getConversationPreview(ChatMessage message) {
        ChatMessageType messageType = message.getMessageType() == null ? ChatMessageType.TEXT : message.getMessageType();
        return switch (messageType) {
            case IMAGE -> "Đã gửi một hình ảnh";
            case PDF -> "Đã gửi một PDF";
            case VIDEO -> "Đã gửi một video";
            case FILE -> "Đã gửi một tệp";
            case TEXT -> message.getContent();
        };
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
}
