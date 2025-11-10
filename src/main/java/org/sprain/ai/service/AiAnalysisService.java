package org.sprain.ai.service;

import org.sprain.ai.dto.ProductAnalysis;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiAnalysisService {

    private final ChatClient chatClient;

    public AiAnalysisService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public ProductAnalysis analyzeReview(String review) {
        String prompt = """
                다음 리뷰를 분석하고 JSON 형식으로 응답해주세요:
                
                리뷰: %s
                
                응답 형식:
                {
                  "sentiment": "positive/neutral/negative",
                  "score": 1-10,
                  "summary": "한 문장 요약"
                }
                """.formatted(review);

        return chatClient.prompt()
            .user(prompt)
            .call()
            .entity(ProductAnalysis.class);
    }
}