package org.sprain.ai.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class PerformanceBasedModelSelector {

    private final ChatModel claudeSonnet;

    private final ChatModel claudeHaiku;

    private final ChatModel gpt4o;

    public PerformanceBasedModelSelector(
            @Qualifier("claudeChatModel") ChatModel claudeSonnet,
            @Qualifier("claudeChatModel") ChatModel claudeHaiku,
            @Qualifier("claudeChatModel") ChatModel gpt4o
    ) {
        this.claudeSonnet = claudeSonnet;
        this.claudeHaiku = claudeHaiku;
        this.gpt4o = gpt4o;
    }

    /**
     * 응답 속도 우선
     */
    public ChatModel selectFastestModel() {
        // Claude Haiku: 가장 빠름
        return claudeHaiku;
    }

    /**
     * 품질 우선
     */
    public ChatModel selectBestQualityModel() {
        // Claude Sonnet: 최고 품질
        return claudeSonnet;
    }

    /**
     * 균형 잡힌 선택
     */
    public ChatModel selectBalancedModel() {
        // GPT-4o: 속도와 품질의 균형
        return gpt4o;
    }

    /**
     * 레이턴시 요구사항에 따라 선택
     */
    public ChatModel selectModelByLatency(int maxLatencyMs) {
        if (maxLatencyMs < 1000) {
            return claudeHaiku;  // ~500ms
        } else if (maxLatencyMs < 3000) {
            return gpt4o;        // ~2000ms
        } else {
            return claudeSonnet; // ~3000ms+
        }
    }
}
