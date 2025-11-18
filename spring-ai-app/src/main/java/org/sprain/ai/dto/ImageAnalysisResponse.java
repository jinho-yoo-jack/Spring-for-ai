package org.sprain.ai.dto;

import lombok.Builder;

@Builder
public record ImageAnalysisResponse(
    String analysis,
    String imageType,
    long imageSize,
    TokenUsage tokenUsage
) {
    public static ImageAnalysisResponse of(String analysis,
                                           String imageType,
                                           long imageSize,
                                           TokenUsage tokenUsage) {
        return ImageAnalysisResponse.builder()
            .analysis(analysis)
            .imageType(imageType)
            .imageSize(imageSize)
            .tokenUsage(tokenUsage)
            .build();
    }
}
