package org.sprain.ai.config;

import lombok.Setter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.*;
import org.springframework.ai.ollama.api.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

@Configuration
@ConfigurationProperties(prefix = "spring.ai.ollama")
@Setter
public class OllamaConfig {
    private String baseUrl;

    @Bean
    public OllamaApi ollamaApi() {
        return new OllamaApi(baseUrl);
    }

    @Bean(name = "ollamaChatModel")
    @Primary
    public OllamaChatModel ollamaChatModel(OllamaApi ollamaApi) {
        return new OllamaChatModel(
            ollamaApi,
            OllamaOptions.builder()
                .withModel("qwen2.5:3b")
                .withTemperature(0.7)
                .build()
        );
    }

    @Bean(name = "ollamaChatClient")
    public ChatClient chatClient(OllamaChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
