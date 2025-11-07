package org.sprain.ai.service;

import org.sprain.ai.dto.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatService {
    ChatResponse chat(String question);

    ChatResponse chatWithHistory(String question, String conversationId);

    Flux<String> chatStream(String question);

    List<Message> getConversationHistory(String conversationId);

    void clearConversationBy(String conversationId);

    void clearAllConversations();
}
