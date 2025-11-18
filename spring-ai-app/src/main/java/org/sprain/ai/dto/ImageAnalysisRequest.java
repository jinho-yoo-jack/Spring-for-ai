package org.sprain.ai.dto;

import org.springframework.web.multipart.MultipartFile;

public record ImageAnalysisRequest(
    String prompt,
    MultipartFile image
) {
    public ImageAnalysisRequest {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt is required");
        }
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("image is required");
        }
    }
}
