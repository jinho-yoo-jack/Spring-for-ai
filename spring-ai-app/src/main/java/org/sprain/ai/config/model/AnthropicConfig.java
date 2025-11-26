package org.sprain.ai.config.model;

import io.modelcontextprotocol.client.McpSyncClient;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.config.RagConfig;
import org.sprain.ai.global.advisor.AdvancedRagAdvisor;
import org.sprain.ai.global.advisor.McpPromptAdvisor;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Map;

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
    public ChatClient anthropicWithMcpToolsChatClient(
            ChatClient.Builder chatClientBuilder,
            List<McpSyncClient> mcpClients,
            Map<String, Advisor> advisorFactory) {

        log.info("=== ChatClient 초기화 (동적 Advisors 적용) ===");
        log.info("등록된 Advisor 수: {}", advisorFactory.size());
        advisorFactory.forEach((name, advisor) ->
                log.info("  - {}: {} (order: {})", name, advisor.getClass().getSimpleName(), advisor.getOrder())
        );

        return chatClientBuilder
                .defaultSystem("""
                        당신은 친절하고 도움이 되는 AI 어시스턴트입니다.
                        사용자의 질문에 정확하고 이해하기 쉽게 답변해주세요.
                        """)
                .defaultAdvisors(advisorFactory.values().toArray(new Advisor[0]))
                .defaultToolCallbacks(
                        SyncMcpToolCallbackProvider.builder()
                                .mcpClients(mcpClients)
                                .build())
                .build();
    }

    @Bean(name = "claudeChatClient")
    public ChatClient anthropicChatClient(
            ChatClient.Builder chatClientBuilder,
            VectorStore vectorStore) {

        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .temperature(0.7)
                .maxTokens(4096)
                .toolCallbacks(List.of())
                .build();

        RagConfig config = RagConfig.builder()
                .topK(10)
                .similarityThreshold(0.75)
                .requireDocuments(false)
                .appendSources(true)
                .build();

        return chatClientBuilder
                .defaultAdvisors(new AdvancedRagAdvisor(vectorStore, config))
                .defaultOptions(options)
                .defaultSystem("""
                        당신은 친절하고 도움이 되는 AI 어시스턴트입니다.
                        사용자의 질문에 정확하고 이해하기 쉽게 답변해주세요.
                        """)
                .build();
    }

}