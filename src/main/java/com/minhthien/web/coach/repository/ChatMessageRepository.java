package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @EntityGraph(attributePaths = {"conversation", "sender", "receiver"})
    Page<ChatMessage> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    @EntityGraph(attributePaths = {"conversation", "sender", "receiver"})
    Optional<ChatMessage> findTopByConversationIdOrderByCreatedAtDesc(Long conversationId);

    @EntityGraph(attributePaths = {"conversation", "sender", "receiver"})
    @Query("""
            select m
            from ChatMessage m
            where m.id in (
                select max(cm.id)
                from ChatMessage cm
                where cm.conversation.id in :conversationIds
                group by cm.conversation.id
            )
            """)
    List<ChatMessage> findLatestMessagesByConversationIds(@Param("conversationIds") Collection<Long> conversationIds);
}
