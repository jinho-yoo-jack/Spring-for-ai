package org.sprain.ai.dto;

public record ChatRequest(
        String message,
        String conversationId
) {
    public ChatRequest {
        if(message == null || message.isBlank()) {
            throw new IllegalArgumentException("message is null or blank");
        }
    }
}
