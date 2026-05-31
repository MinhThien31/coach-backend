package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.request.CreateConversationRequest;
import com.minhthien.web.coach.dto.request.SendConversationMessageRequest;
import com.minhthien.web.coach.dto.response.ApiResponse;
import com.minhthien.web.coach.dto.response.ChatMessageResponse;
import com.minhthien.web.coach.dto.response.ConversationResponse;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.service.ChatService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/conversations")
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateConversationRequest request) {

        ConversationResponse response = chatService.createOrGetConversation(currentUser.getId(), request.getParticipantId());
        return ResponseEntity.ok(ApiResponse.success("Conversation ready", response));
    }

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getMyConversations(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(ApiResponse.success(chatService.getMyConversations(currentUser.getId())));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getMessages(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                chatService.getConversationMessages(currentUser.getId(), conversationId, page, size)
        ));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long conversationId,
            @Valid @RequestBody SendConversationMessageRequest request) {

        ChatMessageResponse response = chatService.sendMessage(currentUser.getId(), conversationId, request.getContent());
        messagingTemplate.convertAndSendToUser(
                response.getReceiverUsername(),
                "/queue/messages",
                ApiResponse.success("New message", response)
        );
        messagingTemplate.convertAndSendToUser(
                response.getSenderUsername(),
                "/queue/messages",
                ApiResponse.success("Message sent", response)
        );
        return ResponseEntity.ok(ApiResponse.success("Message sent", response));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", chatService.getUnreadCount(currentUser.getId()))));
    }

    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<ApiResponse<Void>> markConversationRead(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long conversationId) {

        chatService.markConversationRead(currentUser.getId(), conversationId);
        return ResponseEntity.ok(ApiResponse.success("Conversation marked as read", null));
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long conversationId) {

        chatService.deleteConversation(currentUser.getId(), conversationId);
        return ResponseEntity.ok(ApiResponse.success("Conversation deleted successfully", null));
    }
}
