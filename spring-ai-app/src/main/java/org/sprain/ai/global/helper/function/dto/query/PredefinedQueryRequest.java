package org.sprain.ai.global.helper.function.dto.query;

import java.util.List;

// 미리 정의된 쿼리 요청
public record PredefinedQueryRequest(
    String queryId,
    List<Object> parameters
) {}
