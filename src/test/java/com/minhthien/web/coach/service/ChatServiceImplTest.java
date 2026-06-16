package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.response.ChatMessageResponse;
import com.minhthien.web.coach.entity.ChatMessage;
import com.minhthien.web.coach.entity.Conversation;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.enums.ChatMessageType;
import com.minhthien.web.coach.enums.UserRole;
import com.minhthien.web.coach.exception.UnauthorizedException;
import com.minhthien.web.coach.repository.ChatMessageRepository;
import com.minhthien.web.coach.repository.ConversationRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.service.impl.ChatServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatAttachmentService chatAttachmentService;

    @Test
    void sendAttachmentRejectsUserOutsideConversation() {
        User userOne = user(1L, "learner");
        User userTwo = user(2L, "coach");
        Conversation conversation = Conversation.builder()
                .id(9L)
                .userOne(userOne)
                .userTwo(userTwo)
                .build();
        ChatServiceImpl service = service();

        when(conversationRepository.findById(9L)).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> service.sendAttachment(3L, 9L, file(), null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("not allowed");
        verifyNoInteractions(chatAttachmentService);
    }

    @Test
    void sendAttachmentPersistsMetadataAndReturnsAttachmentFields() {
        User sender = user(1L, "learner");
        User receiver = user(2L, "coach");
        Conversation conversation = Conversation.builder()
                .id(9L)
                .userOne(sender)
                .userTwo(receiver)
                .build();
        ChatAttachmentService.UploadedAttachment upload = new ChatAttachmentService.UploadedAttachment(
                ChatMessageType.IMAGE,
                "https://cdn.example.com/chat/photo.png",
                "coachfinder/chat/photo",
                "photo.png",
                "image/png",
                1234L
        );
        ChatServiceImpl service = service();

        when(conversationRepository.findById(9L)).thenReturn(Optional.of(conversation));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(chatAttachmentService.upload(any())).thenReturn(upload);
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage message = invocation.getArgument(0);
            message.setId(77L);
            return message;
        });

        ChatMessageResponse response = service.sendAttachment(1L, 9L, file(), "  form check  ");

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository).save(captor.capture());
        assertThat(captor.getValue().getMessageType()).isEqualTo(ChatMessageType.IMAGE);
        assertThat(captor.getValue().getAttachmentFileName()).isEqualTo("photo.png");
        assertThat(captor.getValue().getContent()).isEqualTo("form check");
        assertThat(response.getMessageType()).isEqualTo(ChatMessageType.IMAGE);
        assertThat(response.getAttachmentUrl()).isEqualTo("https://cdn.example.com/chat/photo.png");
        assertThat(response.getAttachmentSizeBytes()).isEqualTo(1234L);
    }

    private ChatServiceImpl service() {
        return new ChatServiceImpl(
                conversationRepository,
                chatMessageRepository,
                userRepository,
                chatAttachmentService
        );
    }

    private User user(Long id, String username) {
        return User.builder()
                .id(id)
                .username(username)
                .email(username + "@example.com")
                .fullName(username)
                .role(UserRole.TRAINEES)
                .build();
    }

    private MockMultipartFile file() {
        return new MockMultipartFile("file", "photo.png", "image/png", "image".getBytes());
    }
}
