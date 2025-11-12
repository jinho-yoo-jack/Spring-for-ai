package org.sprain.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CostBasedModelSelector {

    @Qualifier("claudeSonnet")
    private final ChatModel claudeSonnet;     // $3/$15 per 1M tokens

    @Qualifier("claudeHaiku")
    private final ChatModel claudeHaiku;      // $1/$5 per 1M tokens

    @Qualifier("gpt4o")
    private final ChatModel gpt4o;            // $2.5/$10 per 1M tokens

    @Qualifier("gpt35")
    private final ChatModel gpt35;            // $0.5/$1.5 per 1M tokens

    @Qualifier("llama3")
    private final ChatModel llama3;           // 무료 (로컬)

    /**
     * 예상 토큰 수에 따라 모델 선택
     */
    public ChatModel selectModelByTokens(int estimatedTokens) {
        // 매우 짧은 요청 (< 100 tokens)
        if (estimatedTokens < 100) {
            return claudeHaiku;  // 빠르고 저렴
        }

        // 짧은 요청 (< 500 tokens)
        if (estimatedTokens < 500) {
            return gpt35;  // 가장 저렴
        }

        // 중간 길이 (< 2000 tokens)
        if (estimatedTokens < 2000) {
            return gpt4o;  // 균형
        }

        // 긴 요청 (>= 2000 tokens)
        return claudeSonnet;  // 긴 컨텍스트 처리 능력
    }

    /**
     * 예산 제약에 따라 선택
     */
    public ChatModel selectModelByBudget(double maxCostPerRequest) {
        // $0.001 미만
        if (maxCostPerRequest < 0.001) {
            return llama3;  // 무료
        }

        // $0.01 미만
        if (maxCostPerRequest < 0.01) {
            return gpt35;  // 가장 저렴한 클라우드
        }

        // $0.05 미만
        if (maxCostPerRequest < 0.05) {
            return claudeHaiku;  // 저렴하면서 품질 좋음
        }

        // 예산 여유
        return claudeSonnet;  // 최고 품질
    }

    /**
     * 비용 추정
     */
    public double estimateCost(ChatModel model, int inputTokens, int outputTokens) {
        // 모델별 비용 (per 1M tokens)
        Map<String, double[]> pricing = Map.of(
            "claude-sonnet", new double[]{3.0, 15.0},
            "claude-haiku", new double[]{1.0, 5.0},
            "gpt-4o", new double[]{2.5, 10.0},
            "gpt-3.5", new double[]{0.5, 1.5},
            "llama3", new double[]{0.0, 0.0}
        );

        String modelName = getModelName(model);
        double[] costs = pricing.get(modelName);

        double inputCost = (inputTokens / 1_000_000.0) * costs[0];
        double outputCost = (outputTokens / 1_000_000.0) * costs[1];

        return inputCost + outputCost;
    }
}
