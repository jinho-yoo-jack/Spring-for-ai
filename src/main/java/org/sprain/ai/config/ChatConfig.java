package org.sprain.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    /**
     * ChatClient Bean 생성
     * Spring AI가 자동으로 AnthropicChatModel을 주입합니다
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        당신은 친절하고 도움이 되는 AI 어시스턴트입니다.
                        사용자의 질문에 정확하고 이해하기 쉽게 답변해주세요.
                        """)
                .build();
    }
}