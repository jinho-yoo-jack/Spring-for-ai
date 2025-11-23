package org.sprain.ai.config;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * MCP Client Configuration
 * Spring AI 1.1.0-M2
 */
@Slf4j
@Configuration
public class McpClientConfiguration {

    /**
     * ✅ MCP Client 목록 (자동 등록)
     * Spring Boot가 application.yml의 설정을 읽어서 자동으로 McpSyncClient 빈 생성
     */
//    @Bean
    public McpSyncClient companyDocsClient() {
        log.info("=== Company Docs MCP Client 초기화 ===");

        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport.builder("http://localhost:8082")
            .build();
        McpSyncClient client = McpClient.sync(transport)
            .build();
        client.initialize();

        log.info("✅ Company Docs MCP Client 초기화 완료");
        return client;
    }

    /**
     * ✅ 날씨 MCP Client (예시)
     */
//    @Bean
    public McpSyncClient weatherClient() {
        log.info("=== Weather MCP Client 초기화 ===");

        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport.builder("http://localhost:8082")
            .build();

        McpSyncClient client = McpClient.sync(transport)
            .build();
        client.initialize();

        log.info("✅ Weather MCP Client 초기화 완료");
        return client;
    }

    /**
     * ✅ ChatClient with MCP Tools
     *
     * 핵심: List<McpSyncClient>를 주입받아 SyncMcpToolCallbackProvider로 전달
     */
//    @Bean(name = "claudeWithMcpToolsChatClient")
    public ChatClient anthropicWithMcpToolsChatClient(
        ChatClient.Builder chatClientBuilder,
        List<McpSyncClient> mcpClients) {  // ✅ 모든 McpSyncClient 빈을 자동 주입

        log.info("=== ChatClient 초기화 (MCP Tools 통합) ===");
        log.info("연결된 MCP Client 수: {}", mcpClients.size());

        // MCP Client들의 정보 로깅
        for (McpSyncClient client : mcpClients) {
            log.info("  - MCP Client: {}", client.getServerInfo());
        }

        // MCP Client들로부터 Tools를 가져와서 ChatClient에 등록
        return chatClientBuilder
            .defaultSystem("""
                당신은 친절하고 도움이 되는 AI 어시스턴트입니다.
                사용자의 질문에 정확하고 이해하기 쉽게 답변해주세요.
                
                필요한 경우 제공된 Tools를 사용하여 정보를 조회하거나 작업을 수행하세요.
                """)
            .defaultToolCallbacks(
                // ✅ SyncMcpToolCallbackProvider: MCP Tools를 Spring AI Tools로 변환
                SyncMcpToolCallbackProvider.builder()
                    .mcpClients(mcpClients)  // 모든 MCP Client 전달
                    .build()
            )
            .build();
    }
}