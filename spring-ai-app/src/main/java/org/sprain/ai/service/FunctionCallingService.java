package org.sprain.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.global.helper.function.ClaudeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FunctionCallingService {

    private final ChatClient chatClient;
    private final ClaudeTools claudeTools;

    public FunctionCallingService(
        @Qualifier("claudeChatClient") ChatClient chatClient,
        ClaudeTools claudeTools) {
        this.chatClient = chatClient;
        this.claudeTools = claudeTools;
    }

    /**
     * 기본 Function Calling - 모든 도구 사용 가능
     */
    public String chat(String userMessage) {
        log.info("Function Calling 질문: {}", userMessage);

        String response = chatClient.prompt()
            .user(userMessage)
            .tools(claudeTools)  // @Tool 어노테이션이 있는 모든 메서드 사용
            .call()
            .content();

        log.info("Function Calling 응답 완료");
        return response;
    }

    /**
     * 특정 함수만 사용
     * @Tool 어노테이션의 name 속성으로 지정하거나, 메서드명이 기본 이름
     */
    public String chatWithSpecificFunctions(String userMessage, String... functionNames) {
        log.info("제한된 Function Calling: {}", String.join(", ", functionNames));

        // toolNames()를 사용하여 특정 함수만 활성화
        return chatClient.prompt()
            .user(userMessage)
            .toolNames(functionNames)  // Bean에 등록된 함수 이름 지정
            .call()
            .content();
    }

    /**
     * 대화 기억과 함께 사용
     */
    public String chatWithMemory(String conversationId, String userMessage) {
        log.info("대화 ID: {}, 메시지: {}", conversationId, userMessage);

        return chatClient.prompt()
            .user(userMessage)
            .tools(claudeTools)
            .call()
            .content();
    }

    /**
     * 스트리밍 응답 (Function Calling 포함)
     */
    public void chatStream(String userMessage,
                           java.util.function.Consumer<String> onNext,
                           java.util.function.Consumer<Throwable> onError,
                           Runnable onComplete) {
        log.info("Streaming Function Calling 질문: {}", userMessage);

        chatClient.prompt()
            .user(userMessage)
            .tools(claudeTools)
            .stream()
            .content()
            .subscribe(onNext, onError, onComplete);
    }

    /**
     * ChatResponse 전체 정보 받기 (메타데이터 포함)
     */
    public org.springframework.ai.chat.model.ChatResponse chatWithMetadata(String userMessage) {
        log.info("메타데이터 포함 Function Calling: {}", userMessage);

        return chatClient.prompt()
            .user(userMessage)
            .tools(claudeTools)
            .call()
            .chatResponse();
    }

    /**
     * 시스템 메시지와 함께 Function Calling
     */
    public String chatWithSystemMessage(String systemMessage, String userMessage) {
        log.info("System: {}, User: {}", systemMessage, userMessage);

        return chatClient.prompt()
            .system(systemMessage)
            .user(userMessage)
            .tools(claudeTools)
            .call()
            .content();
    }

}