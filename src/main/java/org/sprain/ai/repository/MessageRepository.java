package org.sprain.ai.repository;

import org.sprain.ai.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId);

    void deleteByConversationId(String conversationId);

    long countByConversationId(String conversationId);
}
