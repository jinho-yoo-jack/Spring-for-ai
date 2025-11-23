package org.sprain.ai.global.helper.function.dto.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

// 날씨 조회
public record WeatherRequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("날씨를 조회할 도시명 (예: 서울, 부산)")
    String city,

    @JsonPropertyDescription("온도 단위 (celsius 또는 fahrenheit)")
    String unit
) {}