package org.sprain.ai.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.global.exception.custom.ContextLengthExceededException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
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

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaudeChatService implements ChatService {

    private ChatClient chatClient;
    private final Map<String, ChatClient> allChatClients; // Set<ChatClient> -> Spring이 자동으로 ChatClient Type Bean Set 주입해줘요.
    private Map<String, ChatClient> chatClientMap = new HashMap<>();
    private final Map<String, List<Message>> conversations = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        chatClientMap = allChatClients.entrySet().stream()
                .collect(toMap(
                        element -> element.getKey().split("ChatClient")[0],
                        Map.Entry::getValue
                ));
        log.info("=== Initialized chat clients: {} ===", chatClientMap.keySet());
    }


    @Override
    public ChatResponse chat(String question, String modelName) {
        chatClient = getChatClient(modelName);
//        String response = prompt(question, chatClient);

        // 🔥 chatClientResponse()로 받기 (content + context 모두 필요)
        ChatClient.ChatClientRequestSpec spec = chatClient.prompt()
                .user(question);

        // Response 받기
        ChatClientResponse response = spec.call().chatClientResponse();

        // 기본 응답 내용
        String content = response.chatResponse()
                .getResult()
                .getOutput()
                .getText();

        // 🔥 context에서 formatted_sources 가져오기
        String formattedSources = (String) response.context().get("formatted_sources");

        log.info("=== Content: {} ===", content);
        log.info("=== Formatted Sources: {} ===", formattedSources);

        String responseMessage = content;
        // 출처가 있으면 합치기
        if (formattedSources != null && !formattedSources.isEmpty()) {
            responseMessage += formattedSources;
        }


        return ChatResponse.of(responseMessage, UUID.randomUUID().toString(), modelName);
    }


    @Override
    public ChatResponse chatWithHistory(String question, String conversationId, String modelName) {
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
        return ChatResponse.of(assistantResponse, conversationId, tokenUsage, modelName);
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

    private org.springframework.ai.chat.model.ChatResponse promptWithHistory(String
                                                                                     question, List<Message> history) {
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

    private ChatClient getChatClient(String modelName) {
        return chatClientMap.get(modelName);
    }
}