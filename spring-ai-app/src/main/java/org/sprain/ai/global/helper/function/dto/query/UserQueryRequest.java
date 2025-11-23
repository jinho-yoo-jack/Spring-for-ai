package org.sprain.ai.global.helper.function.dto.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record UserQueryRequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("조회할 사용자 ID")
    String userId
) {
}