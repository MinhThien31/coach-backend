package com.minhthien.web.coach.service.impl;

import com.minhthien.web.coach.dto.request.VideoCallSignalRequest;
import com.minhthien.web.coach.dto.response.CallSessionResponse;
import com.minhthien.web.coach.entity.CallSession;
import com.minhthien.web.coach.entity.Conversation;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.enums.CallSessionStatus;
import com.minhthien.web.coach.enums.CallType;
import com.minhthien.web.coach.exception.BadRequestException;
import com.minhthien.web.coach.exception.ResourceNotFoundException;
import com.minhthien.web.coach.exception.UnauthorizedException;
import com.minhthien.web.coach.repository.CallSessionRepository;
import com.minhthien.web.coach.repository.ConversationRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.service.CallSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CallSessionServiceImpl implements CallSessionService {

    private final CallSessionRepository callSessionRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public VideoCallSignalRequest handleSignal(User currentUser, VideoCallSignalRequest request) {
        String type = normalizeType(request.getType());
        User target = getTargetUser(request.getTargetUsername());
        if (currentUser.getId().equals(target.getId())) {
            throw new BadRequestException("You cannot call yourself");
        }

        request.setType(type);
        request.setSenderUsername(currentUser.getUsername());
        request.setSenderFullName(currentUser.getFullName());

        if ("CALL_INVITE".equals(type)) {
            return handleInvite(currentUser, target, request);
        }

        CallSession session = getCallSessionForUser(request.getCallId(), currentUser.getId());
        validateTargetParticipant(session, target);
        request.setConversationId(session.getConversation().getId());
        request.setCallType(toSignalCallType(session.getCallType()));

        switch (type) {
            case "CALL_ACCEPT" -> accept(session);
            case "CALL_REJECT" -> finish(session, CallSessionStatus.REJECTED);
            case "CALL_CANCEL" -> finish(session, CallSessionStatus.CANCELLED);
            case "CALL_END" -> finish(session, CallSessionStatus.ENDED);
            case "TIMEOUT" -> finish(session, CallSessionStatus.MISSED);
            case "BUSY" -> finish(session, CallSessionStatus.FAILED);
            case "OFFER", "ANSWER", "ICE" -> {
            }
            default -> throw new BadRequestException("Unsupported call signal type: " + type);
        }

        return request;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CallSessionResponse> getConversationCalls(Long currentUserId, Long conversationId, int page, int size) {
        Conversation conversation = getConversationForUser(conversationId, currentUserId);
        return callSessionRepository.findByConversationIdOrderByCreatedAtDesc(
                conversation.getId(),
                PageRequest.of(page, size)
        ).map(session -> mapResponse(session, currentUserId));
    }

    private VideoCallSignalRequest handleInvite(User caller, User callee, VideoCallSignalRequest request) {
        Conversation conversation = getConversationForUser(request.getConversationId(), caller.getId());
        validateConversationParticipant(conversation, callee.getId());

        CallType callType = parseCallType(request.getCallType());
        LocalDateTime now = LocalDateTime.now();
        CallSession saved = callSessionRepository.save(CallSession.builder()
                .conversation(conversation)
                .caller(caller)
                .callee(callee)
                .callType(callType)
                .status(CallSessionStatus.RINGING)
                .startedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build());

        request.setCallId(saved.getId());
        request.setCallType(toSignalCallType(callType));
        request.setConversationId(conversation.getId());
        return request;
    }

    private void accept(CallSession session) {
        if (session.getAcceptedAt() == null) {
            session.setAcceptedAt(LocalDateTime.now());
        }
        session.setStatus(CallSessionStatus.ACCEPTED);
    }

    private void finish(CallSession session, CallSessionStatus status) {
        LocalDateTime endedAt = LocalDateTime.now();
        session.setStatus(status);
        session.setEndedAt(endedAt);

        LocalDateTime base = session.getAcceptedAt() != null ? session.getAcceptedAt() : session.getStartedAt();
        if (base != null) {
            session.setDurationSeconds(Math.max(0, Duration.between(base, endedAt).getSeconds()));
        }
    }

    private Conversation getConversationForUser(Long conversationId, Long userId) {
        if (conversationId == null) {
            throw new BadRequestException("conversationId is required");
        }
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));
        validateConversationParticipant(conversation, userId);
        return conversation;
    }

    private void validateConversationParticipant(Conversation conversation, Long userId) {
        boolean participant = conversation.getUserOne().getId().equals(userId)
                || conversation.getUserTwo().getId().equals(userId);
        if (!participant) {
            throw new UnauthorizedException("You are not allowed to access this conversation");
        }
    }

    private void validateTargetParticipant(CallSession session, User target) {
        boolean validTarget = session.getCaller().getId().equals(target.getId())
                || session.getCallee().getId().equals(target.getId());
        if (!validTarget) {
            throw new UnauthorizedException("Target user is not part of this call");
        }
    }

    private CallSession getCallSessionForUser(Long callId, Long userId) {
        if (callId == null) {
            throw new BadRequestException("callId is required");
        }
        CallSession session = callSessionRepository.findById(callId)
                .orElseThrow(() -> new ResourceNotFoundException("CallSession", "id", callId));
        boolean participant = session.getCaller().getId().equals(userId)
                || session.getCallee().getId().equals(userId);
        if (!participant) {
            throw new UnauthorizedException("You are not allowed to access this call");
        }
        return session;
    }

    private User getTargetUser(String targetUsername) {
        if (targetUsername == null || targetUsername.isBlank()) {
            throw new BadRequestException("targetUsername is required");
        }
        return userRepository.findByUsername(targetUsername.trim())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", targetUsername));
    }

    private CallType parseCallType(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("callType is required");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("video".equals(normalized) || "video-call".equals(normalized)) {
            return CallType.VIDEO;
        }
        if ("audio".equals(normalized) || "voice".equals(normalized) || "audio-call".equals(normalized)) {
            return CallType.AUDIO;
        }
        try {
            return CallType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("callType must be audio or video");
        }
    }

    private String toSignalCallType(CallType callType) {
        return callType == CallType.VIDEO ? "video" : "audio";
    }

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            throw new BadRequestException("type is required");
        }
        return type.trim().toUpperCase(Locale.ROOT);
    }

    private CallSessionResponse mapResponse(CallSession session, Long currentUserId) {
        return CallSessionResponse.builder()
                .id(session.getId())
                .conversationId(session.getConversation().getId())
                .callerId(session.getCaller().getId())
                .callerUsername(session.getCaller().getUsername())
                .callerFullName(session.getCaller().getFullName())
                .calleeId(session.getCallee().getId())
                .calleeUsername(session.getCallee().getUsername())
                .calleeFullName(session.getCallee().getFullName())
                .callType(session.getCallType())
                .status(session.getStatus())
                .startedAt(session.getStartedAt())
                .acceptedAt(session.getAcceptedAt())
                .endedAt(session.getEndedAt())
                .durationSeconds(session.getDurationSeconds())
                .createdAt(session.getCreatedAt())
                .ownCall(session.getCaller().getId().equals(currentUserId))
                .build();
    }
}
