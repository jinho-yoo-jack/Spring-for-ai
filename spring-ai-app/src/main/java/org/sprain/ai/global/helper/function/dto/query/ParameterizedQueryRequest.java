package org.sprain.ai.global.helper.function.dto.query;

import java.util.List;

// 파라미터화된 쿼리 요청
public record ParameterizedQueryRequest(
    String queryTemplate,
    List<Object> parameters
) {}