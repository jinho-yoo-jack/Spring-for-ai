package org.sprain.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.global.exception.custom.ContextLengthExceededException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.sprain.ai.dto.ChatResponse;
import org.sprain.ai.dto.TokenUsage;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ClaudeChatService implements ChatService {

    private final ChatClient chatClient;

    public ClaudeChatService(
        @Qualifier("claudeWithMcpToolsChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    private final Map<String, List<Message>> conversations = new ConcurrentHashMap<>();

    @Override
    public ChatResponse chat(String question) {
        String response = prompt(question);
        return ChatResponse.of(response, UUID.randomUUID().toString());
    }

    @Override
    public ChatResponse chatWithHistory(String question, String conversationId) {
        if (conversationId == null || conversationId.isBlank()) conversationId = UUID.randomUUID().toString();

        List<Message> history = conversations.getOrDefault(conversationId, new ArrayList<>());

        UserMessage userMessage = new UserMessage(question);
        history.add(userMessage);
        org.springframework.ai.chat.model.ChatResponse response = promptWithHistory(question, history);

        String assistantResponse = response.getResult().getOutput().getText();
        if (assistantResponse == null || assistantResponse.isBlank()) {
            throw new IllegalStateException("assistant response is null or blank");
        }
        AssistantMessage assistantMessage = new AssistantMessage(assistantResponse);
        history.add(assistantMessage);
        conversations.put(conversationId, history);

        var metadata = response.getMetadata();
        TokenUsage tokenUsage = null;
        if (metadata != null && metadata.getUsage() != null) {
            var usage = metadata.getUsage();
            tokenUsage = new TokenUsage(usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
        }
        return ChatResponse.of(assistantResponse, conversationId, tokenUsage);
    }

    @Override
    public Flux<String> chatStream(String question) {
        return promptStream(question);
    }

    @Override
    public List<Message> getConversationHistory(String conversationId) {
        return conversations.getOrDefault(conversationId, Collections.emptyList());
    }

    @Override
    public void clearConversationBy(String conversationId) {
        conversations.remove(conversationId);
    }

    @Override
    public void clearAllConversations() {
        conversations.clear();
    }

    private String prompt(String question) {
        return chatClient.prompt()
            .user(question)
            .call()
            .content();
    }

    private org.springframework.ai.chat.model.ChatResponse promptWithHistory(String question, List<Message> history) {
        try {
            return chatClient.prompt()
                .messages(history)
                .user(question)
                .call()
                .chatResponse();
        } catch (ContextLengthExceededException e) {
            throw new ContextLengthExceededException(e.getMessage());
        }
    }

    private Flux<String> promptStream(String question) {
        return chatClient.prompt()
            .user(question)
            .stream()
            .content();
    }
}