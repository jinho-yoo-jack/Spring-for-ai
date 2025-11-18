package org.sprain.ai.repository;

import org.sprain.ai.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    List<Conversation> findByUserIdOrderByLastMessageAtDesc(String userId);

    void deleteByUserId(String userId);
}
