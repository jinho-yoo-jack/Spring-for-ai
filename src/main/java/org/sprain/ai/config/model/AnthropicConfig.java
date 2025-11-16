package org.sprain.ai.config.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "spring.ai.anthropic")
@Slf4j
@Setter
public class AnthropicConfig {
    private String apiKey;

    @PostConstruct
    public void init() {
        log.debug("ClaudeChatService init");
        log.debug("Claude apiKey: " + apiKey);
    }

    @Bean
    public AnthropicApi anthropicApi() {
        AnthropicApi.Builder builder = new AnthropicApi.Builder();
        builder.apiKey(apiKey);
        return builder.build();
    }

    @Bean(name = "claudeChatModel")
    @Primary
    public AnthropicChatModel anthropicChatModel(AnthropicApi anthropicApi) {
        String modelName = "claude-sonnet-4-20250514";
        return AnthropicChatModel.builder()
            .anthropicApi(anthropicApi)
            .defaultOptions(
                AnthropicChatOptions.builder()
                    .model(modelName)
                    .temperature(0.5)
                    .maxTokens(2048)
                    .build())
            .build();
    }

    /**
     * ChatClient with MCP Tools
     * <p>
     * SSE 방식:
     * 1. application.yml에서 spring.ai.mcp.client.sse.connections 설정
     * 2. Spring AI가 자동으로 McpSyncClient 생성 (HTTP SSE로 연결)
     * 3. SyncMcpToolCallbackProvider로 MCP Tools 추출
     * 4. ChatClient에 등록
     */
    @Bean(name = "claudeWithMcpToolsChatClient")
    public ChatClient chatClient(
        ChatClient.Builder chatClientBuilder,
        List<McpSyncClient> mcpClients) {

        // MCP Client들로부터 Tools를 가져와서 ChatClient에 등록
        return chatClientBuilder
            .defaultSystem("""
                당신은 친절하고 도움이 되는 AI 어시스턴트입니다.
                사용자의 질문에 정확하고 이해하기 쉽게 답변해주세요.
                """)
            .defaultToolCallbacks(
                SyncMcpToolCallbackProvider.builder()
                    .mcpClients(mcpClients)
                    .build())
            .build();
    }

//    @Bean(name = "claudeChatClient")
//    public ChatClient chatClient(
//        ChatClient.Builder chatClientBuilder) {
//
//        // MCP Client들로부터 Tools를 가져와서 ChatClient에 등록
//        return chatClientBuilder
//            .defaultSystem("""
//                당신은 친절하고 도움이 되는 AI 어시스턴트입니다.
//                사용자의 질문에 정확하고 이해하기 쉽게 답변해주세요.
//                """)
//            .build();
//    }

}