package com.minhthien.web.coach.entity;

import com.minhthien.web.coach.enums.ChatMessageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false, length = 2000)
    private String content;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20, columnDefinition = "varchar(20) default 'TEXT'")
    private ChatMessageType messageType = ChatMessageType.TEXT;

    @Column(name = "attachment_url", length = 1000)
    private String attachmentUrl;

    @Column(name = "attachment_public_id", length = 255)
    private String attachmentPublicId;

    @Column(name = "attachment_file_name", length = 255)
    private String attachmentFileName;

    @Column(name = "attachment_mime_type", length = 120)
    private String attachmentMimeType;

    @Column(name = "attachment_size_bytes")
    private Long attachmentSizeBytes;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private Boolean read = false;

    private LocalDateTime readAt;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (read == null) {
            read = false;
        }
        if (messageType == null) {
            messageType = ChatMessageType.TEXT;
        }
    }
}
