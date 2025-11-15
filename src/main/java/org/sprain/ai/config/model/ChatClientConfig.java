package org.sprain.ai.config.model;

import io.modelcontextprotocol.client.McpSyncClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Slf4j
@Configuration
public class ChatClientConfig {

    /**
     * Claude ChatClient
     */
    @Bean(name = "claudeChatClient")
    @Primary
    public ChatClient claudeChatClient(
        AnthropicChatModel anthropicChatModel,
        @Autowired(required = false) ToolCallbackProvider weatherToolCallbackProvider,
        @Autowired(required = false) List<McpSyncClient> mcpSyncClients
    ) {
        log.info("=== Claude ChatClient 초기화 ===");

        ChatClient.Builder builder = ChatClient.builder(anthropicChatModel);

        // 1. Weather Function Tool 등록
        if (weatherToolCallbackProvider != null) {
            log.info("✅ Weather Tool 등록");
            builder = builder.defaultToolCallbacks(weatherToolCallbackProvider);
        }

        // 2. MCP Tools 등록 (Builder 패턴 사용)
        if (mcpSyncClients != null && !mcpSyncClients.isEmpty()) {
            log.info("✅ MCP Clients: {} 개", mcpSyncClients.size());

            // ✅ Builder 패턴으로 생성 (Deprecated 회피)
            SyncMcpToolCallbackProvider mcpToolProvider = SyncMcpToolCallbackProvider.builder()
                .mcpClients(mcpSyncClients)
                .build();

            builder = builder.defaultToolCallbacks(mcpToolProvider);

            // MCP Client 정보 로깅
            mcpSyncClients.forEach(client -> {
                log.info("  - MCP Client: {}", client.getClass().getSimpleName());
            });
        }

        ChatClient chatClient = builder.build();
        log.info("=== Claude ChatClient 초기화 완료 ===");

        return chatClient;
    }

    /**
     * Ollama ChatClient
     */
    @Bean(name = "ollamaChatClient")
    public ChatClient ollamaChatClient(
        OllamaChatModel ollamaChatModel,
        @Autowired(required = false) ToolCallbackProvider weatherToolCallbackProvider,
        @Autowired(required = false) List<McpSyncClient> mcpSyncClients
    ) {
        log.info("=== Ollama ChatClient 초기화 ===");

        ChatClient.Builder builder = ChatClient.builder(ollamaChatModel);

        // Weather Tool
        if (weatherToolCallbackProvider != null) {
            log.info("✅ Weather Tool 등록");
            builder = builder.defaultToolCallbacks(weatherToolCallbackProvider);
        }

        // MCP Tools (Builder 패턴)
        if (mcpSyncClients != null && !mcpSyncClients.isEmpty()) {
            log.info("✅ MCP Clients: {} 개", mcpSyncClients.size());

            SyncMcpToolCallbackProvider mcpToolProvider = SyncMcpToolCallbackProvider.builder()
                .mcpClients(mcpSyncClients)
                .build();

            builder = builder.defaultToolCallbacks(mcpToolProvider);
        }

        log.info("=== Ollama ChatClient 초기화 완료 ===");
        return builder.build();
    }

    /**
     * MCP 전용 ChatClient
     */
    @Bean(name = "ollamaWithMcpToolChatClient")
    public ChatClient mcpOnlyChatClient(
        AnthropicChatModel anthropicChatModel,
        @Autowired(required = false) List<McpSyncClient> mcpSyncClients
    ) {
        log.info("=== MCP 전용 ChatClient 초기화 ===");

        if (mcpSyncClients == null || mcpSyncClients.isEmpty()) {
            log.warn("⚠️ MCP Client가 없습니다!");
            return ChatClient.builder(anthropicChatModel).build();
        }

        log.info("✅ MCP Clients: {} 개", mcpSyncClients.size());

        // Builder 패턴 사용
        SyncMcpToolCallbackProvider mcpToolProvider = SyncMcpToolCallbackProvider.builder()
            .mcpClients(mcpSyncClients)
            .build();

        return ChatClient.builder(anthropicChatModel)
            .defaultToolCallbacks(mcpToolProvider)
            .build();
    }
}