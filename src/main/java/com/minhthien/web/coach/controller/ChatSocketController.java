package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.request.SendChatMessageRequest;
import com.minhthien.web.coach.dto.response.ApiResponse;
import com.minhthien.web.coach.dto.response.ChatMessageResponse;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.exception.UnauthorizedException;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatSocketController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Valid @Payload SendChatMessageRequest request, Principal principal) {
        if (!(principal instanceof Authentication authentication) || !(authentication.getPrincipal() instanceof User userDetails)) {
            throw new UnauthorizedException("WebSocket user is not authenticated");
        }

        User currentUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UnauthorizedException("Current user not found"));

        ChatMessageResponse response = chatService.sendMessage(
                currentUser.getId(),
                request.getConversationId(),
                request.getContent()
        );

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
    }
}
