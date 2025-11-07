package org.sprain.ai.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatResponse(
        String message,
        String conversationId,
        LocalDateTime timestamp,
        TokenUsage tokenUsage
) {
    public static ChatResponse of(String message, String conversationId, LocalDateTime timestamp, TokenUsage tokenUsage) {
        return ChatResponse.builder()
                .message(message)
                .conversationId(conversationId)
                .timestamp(timestamp)
                .tokenUsage(tokenUsage)
                .build();
    }

    public static ChatResponse of(String message, String conversationId, TokenUsage tokenUsage) {
        return ChatResponse.of(message, conversationId, LocalDateTime.now(), tokenUsage);
    }

    public static ChatResponse of(String message, String conversationId) {
        return ChatResponse.of(message, conversationId, LocalDateTime.now(), null);
    }

}
