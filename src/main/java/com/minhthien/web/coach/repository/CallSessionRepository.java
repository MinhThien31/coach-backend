package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.CallSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CallSessionRepository extends JpaRepository<CallSession, Long> {

    @EntityGraph(attributePaths = {"conversation", "caller", "callee"})
    Page<CallSession> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);
}
