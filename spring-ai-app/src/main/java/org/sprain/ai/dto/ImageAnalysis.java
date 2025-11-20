package org.sprain.ai.dto;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Builder
public record ImageAnalysis(
        String prompt,
        byte[] imageBytes,
        String contentType
) {
    public static ImageAnalysis of(String prompt, MultipartFile imageFile) throws IOException {
        return ImageAnalysis.builder()
                .prompt(prompt)
                .imageBytes(imageFile.getBytes())
                .contentType(imageFile.getContentType())
                .build();
    }

    public static ImageAnalysis of(ImageAnalysisRequest request) throws IOException {
        return ImageAnalysis.of(request.prompt(), request.image());
    }

    public static ImageAnalysis of(MultipartFile imageFile) throws IOException {
        return ImageAnalysis.of("", imageFile);
    }
}
