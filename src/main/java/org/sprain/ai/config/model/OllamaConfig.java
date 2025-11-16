package org.sprain.ai.config.model;

import io.modelcontextprotocol.client.McpSyncClient;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "spring.ai.ollama")
@Setter
@Profile("ollama")
public class OllamaConfig {

    private String baseUrl;

    /**
     * OllamaApi 생성 (Builder 패턴)
     * 1.1.0-M2에서는 Builder를 사용해야 함
     */
    @Bean
    public OllamaApi ollamaApi() {
        log.info("=== OllamaApi 생성 ===");
        log.info("Base URL: {}", baseUrl);

        // ✅ Builder 패턴 사용
        return OllamaApi.builder()
            .baseUrl(baseUrl)
            .build();
    }

    /**
     * OllamaChatModel 생성
     */
    @Bean(name = "ollamaChatModel")
    public OllamaChatModel ollamaChatModel(
        OllamaApi ollamaApi) {
        log.info("=== OllamaChatModel 생성 ===");

        // 기본 옵션 설정
        OllamaChatOptions options = OllamaChatOptions.builder()
            .model("qwen2.5:3b")              // 모델 이름
            .temperature(0.7)                 // 창의성 (0.0 ~ 1.0)
            .numPredict(1000)                 // 최대 생성 토큰 수
            .topK(40)                         // Top-K 샘플링
            .topP(0.9)                        // Top-P (nucleus) 샘플링
            .repeatPenalty(1.1)               // 반복 방지
            .build();


        log.info("Ollama 모델: {}", options.getModel());
        log.info("Temperature: {}", options.getTemperature());

        return OllamaChatModel.builder()
            .ollamaApi(ollamaApi)
            .defaultOptions(options)
            .build();
    }

    /**
     * Ollama ChatClient 생성
     */
    @Bean(name = "ollamaChatClient")
    public ChatClient ollamaChatClient(
        ChatClient.Builder chatClientBuilder,
        List<McpSyncClient> mcpClients) {
        return chatClientBuilder
            .defaultToolCallbacks(
                SyncMcpToolCallbackProvider.builder()
                    .mcpClients(mcpClients)
                    .build())
            .build();
    }
}