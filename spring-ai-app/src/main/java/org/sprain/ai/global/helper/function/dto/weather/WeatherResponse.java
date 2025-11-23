package org.sprain.ai.global.helper.function.dto.weather;

import java.time.LocalDateTime;

public record WeatherResponse(
    String city,
    int temperature,
    String condition,
    LocalDateTime timestamp
) {
}