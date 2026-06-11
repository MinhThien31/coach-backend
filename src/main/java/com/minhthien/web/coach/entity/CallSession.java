package com.minhthien.web.coach.entity;

import com.minhthien.web.coach.enums.CallSessionStatus;
import com.minhthien.web.coach.enums.CallType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "call_sessions",
        indexes = {
                @Index(name = "idx_call_session_conversation_created", columnList = "conversation_id, created_at"),
                @Index(name = "idx_call_session_caller", columnList = "caller_id"),
                @Index(name = "idx_call_session_callee", columnList = "callee_id"),
                @Index(name = "idx_call_session_status", columnList = "status")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caller_id", nullable = false)
    private User caller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "callee_id", nullable = false)
    private User callee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CallType callType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CallSessionStatus status;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime acceptedAt;

    private LocalDateTime endedAt;

    private Long durationSeconds;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
