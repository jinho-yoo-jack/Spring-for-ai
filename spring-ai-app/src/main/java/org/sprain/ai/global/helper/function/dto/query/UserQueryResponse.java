package org.sprain.ai.global.helper.function.dto.query;

public record UserQueryResponse(
    String userId,
    String name,
    String email,
    String phone
) {
}