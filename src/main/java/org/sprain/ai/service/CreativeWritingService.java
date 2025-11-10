package org.sprain.ai.service;

import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class CreativeWritingService {

    private final ChatClient chatClient;

    public CreativeWritingService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String generateCreativeStory(String topic) {
        return chatClient.prompt()
            .user("다음 주제로 창의적인 이야기를 작성해주세요: " + topic)
            .options(AnthropicChatOptions.builder()
                .model("claude-3-5-sonnet-20241022")
                .temperature(0.9)  // 높은 창의성
                .maxTokens(500)
                .build())
            .call()
            .content();
    }

    public String generateFactualSummary(String text) {
        return chatClient.prompt()
            .user("다음 텍스트를 요약해주세요: " + text)
            .options(AnthropicChatOptions.builder()
                .model("claude-3-5-sonnet-20241022")
                .temperature(0.1)  // 낮은 창의성, 높은 정확성
                .maxTokens(200)
                .build())
            .call()
            .content();
    }
}