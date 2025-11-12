package org.sprain.ai.config;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        return new AnthropicApi(apiKey);
    }

    @Bean(name = "claudeChatModel")
    public AnthropicChatModel anthropicChatModel(AnthropicApi anthropicApi) {
        return new AnthropicChatModel(anthropicApi,
                AnthropicChatOptions.builder()
                        .withModel("claude-3-5-sonnet-20241022")
                        .withTemperature(0.7)
                        .withMaxTokens(2048)
                        .build()
        );
    }

    @Bean(name = "claudeChatClient")
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
            .defaultSystem("""
                당신은 친절하고 도움이 되는 AI 어시스턴트입니다.
                사용자의 질문에 정확하고 이해하기 쉽게 답변해주세요.
                """)
            .build();
    }
}