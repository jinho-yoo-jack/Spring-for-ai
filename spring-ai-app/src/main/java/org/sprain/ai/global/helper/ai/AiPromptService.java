package org.sprain.ai.global.helper.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiPromptService {

    private final ChatClient chatClient;

    public AiPromptService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String generateProductDescription(String productName, String features) {
        String template = """
                다음 제품에 대한 마케팅 문구를 작성해주세요:
                
                제품명: {productName}
                주요 특징: {features}
                
                요구사항:
                - 100자 이내로 간결하게
                - 감성적이고 매력적인 표현 사용
                - 고객의 문제 해결 강조
                """;

        return chatClient.prompt()
            .user(u -> u.text(template)
                .param("productName", productName)
                .param("features", features))
            .call()
            .content();
    }
}