package org.sprain.ai.dto;

import lombok.Builder;
import org.sprain.ai.dto.TokenUsage;

@Builder
public record ImageAnalysisResponseV2<T>(
        T analysis,
        String imageType,
        long imageSize,
        TokenUsage tokenUsage
) {
    public static <T> ImageAnalysisResponseV2<T> of(T analysis,
                                                  String imageType,
                                                  long imageSize,
                                                  TokenUsage tokenUsage) {
        return ImageAnalysisResponseV2.<T>builder()
                .analysis(analysis)
                .imageType(imageType)
                .imageSize(imageSize)
                .tokenUsage(tokenUsage)
                .build();
    }
}
