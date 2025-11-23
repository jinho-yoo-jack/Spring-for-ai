package org.sprain.ai.global.helper.function.dto.query;

import java.util.List;

// 쿼리 검증 응답
public record QueryValidationResponse(
    boolean isValid,
    String message,
    List<String> issues
) {}