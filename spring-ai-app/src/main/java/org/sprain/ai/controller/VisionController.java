package org.sprain.ai.controller;

import lombok.RequiredArgsConstructor;
import org.sprain.ai.dto.ImageAnalysisResponse;
import org.sprain.ai.service.VisionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/vision")
@RequiredArgsConstructor
public class VisionController {

    private final VisionService visionService;

    /**
     * 이미지 분석 (커스텀 프롬프트)
     * POST /api/vision/analyze
     */
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageAnalysisResponse> analyzeImage(
        @RequestParam("prompt") String prompt,
        @RequestParam("image") MultipartFile image) {

        ImageAnalysisResponse response = visionService.analyzeImage(prompt, image);
        return ResponseEntity.ok(response);
    }

    /**
     * OCR - 이미지에서 텍스트 추출
     * POST /api/vision/ocr
     */
    @PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> extractText(
        @RequestParam("image") MultipartFile image) {

        String text = visionService.extractText(image);
        return ResponseEntity.ok(Map.of("text", text));
    }

    /**
     * 이미지 상세 설명
     * POST /api/vision/describe
     */
    @PostMapping(value = "/describe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> describeImage(
        @RequestParam("image") MultipartFile image) {

        String description = visionService.describeImage(image);
        return ResponseEntity.ok(Map.of("description", description));
    }

    /**
     * 차트/그래프 분석
     * POST /api/vision/chart
     */
    @PostMapping(value = "/chart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> analyzeChart(
        @RequestParam("image") MultipartFile image) {

        String analysis = visionService.analyzeChart(image);
        return ResponseEntity.ok(Map.of("analysis", analysis));
    }

    /**
     * 두 이미지 비교
     * POST /api/vision/compare
     */
    @PostMapping(value = "/compare", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> compareImages(
        @RequestParam("image1") MultipartFile image1,
        @RequestParam("image2") MultipartFile image2) {

        String comparison = visionService.compareImages(image1, image2);
        return ResponseEntity.ok(Map.of("comparison", comparison));
    }
}
