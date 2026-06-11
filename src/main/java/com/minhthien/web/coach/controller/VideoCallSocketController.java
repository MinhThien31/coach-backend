package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.request.VideoCallSignalRequest;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.exception.UnauthorizedException;
import com.minhthien.web.coach.service.CallSessionService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class VideoCallSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final CallSessionService callSessionService;

    public VideoCallSocketController(SimpMessagingTemplate messagingTemplate, CallSessionService callSessionService) {
        this.messagingTemplate = messagingTemplate;
        this.callSessionService = callSessionService;
    }

    @MessageMapping("/call.signal")
    public void handleSignal(@Payload VideoCallSignalRequest request, Principal principal) {
        if (!(principal instanceof Authentication authentication) || !(authentication.getPrincipal() instanceof User userDetails)) {
            throw new UnauthorizedException("WebSocket user is not authenticated");
        }

        VideoCallSignalRequest response = callSessionService.handleSignal(userDetails, request);

        messagingTemplate.convertAndSendToUser(
                response.getTargetUsername(),
                "/queue/call",
                response
        );

        if ("CALL_INVITE".equals(response.getType())) {
            messagingTemplate.convertAndSendToUser(
                    userDetails.getUsername(),
                    "/queue/call",
                    response
            );
        }
    }
}
