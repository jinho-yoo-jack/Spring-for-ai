package org.sprain.ai.global.helper.function.dto.common;

public record CurrentTimeResponse(
    String isoFormat,
    String readableFormat
) {
}