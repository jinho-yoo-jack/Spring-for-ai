package org.sprain.ai.dto;

public record TokenUsage(
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens
) {
}
