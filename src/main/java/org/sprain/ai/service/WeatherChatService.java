package org.sprain.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * MCP Client: ChatClient에서 MCP Tool 사용
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherChatService {

    @Qualifier("claudeWithMcpToolsChatClient")
    private final ChatClient chatClient;

    /**
     * MCP Tool을 활용한 채팅
     */
    public String chat(String userMessage) {
        log.info("💬 [MCP Client] 사용자 메시지: {}", userMessage);

        // MCP Tools를 포함한 ChatClient 생성
//        ChatClient chatClient = chatClientBuilder
//            .defaultTools(toolCallbackProvider)  // ⭐ MCP Tools 자동 주입
//            .build();

        // LLM에게 질문 (필요시 Tool 자동 호출)
        String response = chatClient.prompt()
            .user(userMessage)
            .call()
            .content();

        log.info("🤖 [MCP Client] AI 응답: {}", response);

        return response;
    }
}