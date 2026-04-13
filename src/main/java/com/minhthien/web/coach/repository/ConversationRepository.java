package com.minhthien.web.coach.repository;

import com.minhthien.web.coach.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByUserOneIdAndUserTwoId(Long userOneId, Long userTwoId);

    @Query("""
            select c
            from Conversation c
            where c.userOne.id = :userId or c.userTwo.id = :userId
            order by c.updatedAt desc
            """)
    List<Conversation> findAllByParticipantIdOrderByUpdatedAtDesc(@Param("userId") Long userId);
}
