package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.request.VideoCallSignalRequest;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.exception.UnauthorizedException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class VideoCallSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public VideoCallSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/call.signal")
    public void handleSignal(@Payload VideoCallSignalRequest request, Principal principal) {
        if (!(principal instanceof Authentication authentication) || !(authentication.getPrincipal() instanceof User userDetails)) {
            throw new UnauthorizedException("WebSocket user is not authenticated");
        }

        request.setSenderUsername(userDetails.getUsername());
        
        // Relay the WebRTC signaling message to the target user
        messagingTemplate.convertAndSendToUser(
                request.getTargetUsername(),
                "/queue/call",
                request
        );
    }
}
