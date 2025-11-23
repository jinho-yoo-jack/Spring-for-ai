package org.sprain.ai.global.helper.function.dto.calulator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record CalculatorRequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("첫 번째 숫자")
    double a,

    @JsonProperty(required = true)
    @JsonPropertyDescription("두 번째 숫자")
    double b,

    @JsonProperty(required = true)
    @JsonPropertyDescription("연산 종류: add, subtract, multiply, divide")
    String operation
) {}