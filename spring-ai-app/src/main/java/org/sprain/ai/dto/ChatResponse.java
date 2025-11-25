package org.sprain.ai.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatResponse(
        String message,
        String conversationId,
        LocalDateTime timestamp,
        TokenUsage tokenUsage,
        String modelName
) {
    public static ChatResponse of(String message, String conversationId, LocalDateTime timestamp, TokenUsage tokenUsage, String modelName) {
        return ChatResponse.builder()
                .message(message)
                .conversationId(conversationId)
                .timestamp(timestamp)
                .tokenUsage(tokenUsage)
                .modelName(modelName)
                .build();
    }

    public static ChatResponse of(String message, String conversationId, TokenUsage tokenUsage, String modelName) {
        return ChatResponse.of(message, conversationId, LocalDateTime.now(), tokenUsage, modelName);
    }

    public static ChatResponse of(String message, String conversationId, String modelName) {
        return ChatResponse.of(message, conversationId, LocalDateTime.now(), null, modelName);
    }

}
