package org.sprain.ai.global.helper.function.dto.query;

import java.util.*;

// 데이터베이스 쿼리 응답
public record DatabaseQueryResponse(
    boolean success,
    String message,
    List<Map<String, Object>> data,
    int rowCount
) {
}