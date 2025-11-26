package org.sprain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.dto.ImageAnalysis;
import org.sprain.ai.dto.ImageAnalysisResponseV2;
import org.sprain.ai.dto.TokenUsage;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class VisionServiceV2 {

    private static final String DEFAULT_CONTENT_TYPE = "image/jpeg";
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public VisionServiceV2(@Qualifier("claudeChatClient")
                           ChatClient chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 이미지 분석 - 제네릭 타입 지원
     */
    public <T> ImageAnalysisResponseV2<T> analyzeImage(
            ImageAnalysis imageAnalysis,
            Class<T> targetType
    ) {
        try {
            // 1. 입력 검증
            validateImageAnalysis(imageAnalysis);

            // 2. 이미지 정보 추출
            byte[] imageBytes = imageAnalysis.imageBytes();
            String contentType = Optional.ofNullable(imageAnalysis.contentType())
                    .orElse(DEFAULT_CONTENT_TYPE);

            // 3. UserMessage 생성
            UserMessage userMessage = createUserMessage(
                    imageBytes,
                    contentType,
                    imageAnalysis.prompt()
            );

            // 4. API 호출 (항상 ChatResponse로 받음)
            ChatResponse chatResponse = callChatApi(userMessage);

            // 5. 응답 텍스트 추출
            String rawText = chatResponse.getResult().getOutput().getText();
            log.debug("Vision API 원본 응답: {}", rawText);

            // 6. 토큰 사용량 추출
            TokenUsage tokenUsage = extractTokenUsage(chatResponse.getMetadata());

            // 7. 타겟 타입으로 변환
            T analysis = convertToTargetType(rawText, targetType);

            log.info("이미지 분석 완료 - 타입: {}, 크기: {}, 토큰: {}",
                    targetType.getSimpleName(),
                    imageBytes.length,
                    tokenUsage);

            return ImageAnalysisResponseV2.of(
                    analysis,
                    contentType,
                    imageBytes.length,
                    tokenUsage
            );

        } catch (Exception e) {
            log.error("이미지 분석 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 분석 중 오류 발생", e);
        }
    }

    /**
     * 이미지에서 텍스트 추출 (OCR)
     */
    public String extractText(MultipartFile imageFile) throws IOException {
        String prompt = """
                이미지에 있는 모든 텍스트를 정확하게 추출해주세요.
                텍스트만 출력하고, 다른 설명은 필요 없습니다.
                """;

        ImageAnalysisResponseV2<String> response = analyzeImage(
                ImageAnalysis.of(prompt, imageFile),
                String.class
        );

        return response.analysis();
    }

    /**
     * 이미지 상세 설명 생성
     */
    public String describeImage(MultipartFile imageFile) throws IOException {
        String prompt = """
                이 이미지를 다음 형식으로 상세히 설명해주세요:
                
                1. 전체적인 장면
                2. 주요 객체들
                3. 색상과 분위기
                4. 특이사항
                """;

        ImageAnalysisResponseV2<String> response = analyzeImage(
                ImageAnalysis.of(prompt, imageFile),
                String.class
        );

        return response.analysis();
    }

    /**
     * 차트/그래프 분석
     */
    public String analyzeChart(MultipartFile imageFile) throws IOException {
        String prompt = """
                이 차트/그래프를 분석해주세요:
                
                1. 차트 유형 (막대, 선, 파이 등)
                2. 주요 데이터 포인트
                3. 트렌드와 패턴
                4. 인사이트와 결론
                """;

        ImageAnalysisResponseV2<String> response = analyzeImage(
                ImageAnalysis.of(prompt, imageFile),
                String.class
        );

        return response.analysis();
    }

    /**
     * 이미지 비교 분석
     */
    public String compareImages(MultipartFile image1, MultipartFile image2) {
        try {
            // 첫 번째 이미지 처리
            byte[] bytes1 = image1.getBytes();
            Media media1 = new Media(
                    MimeTypeUtils.parseMimeType(
                            Optional.ofNullable(image1.getContentType())
                                    .orElse(DEFAULT_CONTENT_TYPE)
                    ),
                    new ByteArrayResource(bytes1)
            );

            // 두 번째 이미지 처리
            byte[] bytes2 = image2.getBytes();
            Media media2 = new Media(
                    MimeTypeUtils.parseMimeType(
                            Optional.ofNullable(image2.getContentType())
                                    .orElse(DEFAULT_CONTENT_TYPE)
                    ),
                    new ByteArrayResource(bytes2)
            );

            // 프롬프트와 함께 두 이미지 전달
            String prompt = """
                    두 이미지를 비교하여 다음을 설명해주세요:
                    
                    1. 공통점
                    2. 차이점
                    3. 각 이미지의 특징
                    """;

            // UserMessage 생성
            UserMessage userMessage = UserMessage.builder()
                    .media(List.of(media1, media2))
                    .text(prompt)
                    .build();

            ChatResponse response = chatClient.prompt()
                    .messages(userMessage)
                    .call()
                    .chatResponse();

            return response.getResult().getOutput().getText();

        } catch (IOException e) {
            log.error("이미지 비교 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 비교 중 오류 발생", e);
        }
    }

    // === Private 헬퍼 메서드 ===

    /**
     * 입력 검증
     */
    private void validateImageAnalysis(ImageAnalysis imageAnalysis) {
        if (imageAnalysis == null) {
            throw new IllegalArgumentException("ImageAnalysis는 null일 수 없습니다");
        }
        if (imageAnalysis.imageBytes() == null || imageAnalysis.imageBytes().length == 0) {
            throw new IllegalArgumentException("이미지 데이터가 비어있습니다");
        }
        if (imageAnalysis.prompt() == null || imageAnalysis.prompt().isBlank()) {
            throw new IllegalArgumentException("프롬프트가 비어있습니다");
        }
    }

    /**
     * UserMessage 생성
     */
    private UserMessage createUserMessage(
            byte[] imageBytes,
            String contentType,
            String prompt
    ) {
        Media media = new Media(
                MimeTypeUtils.parseMimeType(contentType),
                new ByteArrayResource(imageBytes)
        );

        return UserMessage.builder()
                .media(List.of(media))
                .text(prompt)
                .build();
    }

    /**
     * Chat API 호출
     */
    private ChatResponse callChatApi(UserMessage userMessage) {
        return chatClient.prompt()
                .messages(userMessage)
                .call()
                .chatResponse();
    }

    /**
     * 타입 변환
     */
    @SuppressWarnings("unchecked")
    private <T> T convertToTargetType(String text, Class<T> targetType) {
        // String 타입 요청
        if (targetType == String.class) {
            return (T) text;
        }

        // JSON 파싱 시도
        try {
            // JSON 응답 정제
            String cleanedJson = cleanJsonResponse(text);
            return objectMapper.readValue(cleanedJson, targetType);
        } catch (Exception e) {
            log.error("JSON 파싱 실패 (타입: {}): {}", targetType.getSimpleName(), e.getMessage());
            log.debug("파싱 실패한 텍스트: {}", text);
            throw new RuntimeException("응답 변환 실패: " + e.getMessage(), e);
        }
    }

    /**
     * JSON 응답 정제
     */
    private String cleanJsonResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new IllegalArgumentException("응답이 비어있습니다");
        }

        log.debug("정제 전 응답: {}", response);

        // 1. 마크다운 코드 블록 제거
        response = response.replaceAll("```json\\s*", "");
        response = response.replaceAll("```\\s*", "");

        // 2. 모든 백틱 제거
        response = response.replace("`", "");

        // 3. 앞뒤 공백 제거
        response = response.trim();

        // 4. JSON 시작/끝 위치 찾기
        int jsonStart = response.indexOf("{");
        int jsonEnd = response.lastIndexOf("}");

        if (jsonStart == -1 || jsonEnd == -1 || jsonStart >= jsonEnd) {
            log.error("유효한 JSON을 찾을 수 없습니다. 응답: {}", response);
            throw new IllegalArgumentException("유효한 JSON을 찾을 수 없습니다");
        }

        // 5. JSON 부분만 추출
        response = response.substring(jsonStart, jsonEnd + 1);

        log.debug("정제 후 응답: {}", response);

        return response;
    }

    /**
     * 토큰 사용량 추출
     */
    private TokenUsage extractTokenUsage(ChatResponseMetadata metadata) {
        if (metadata == null || metadata.getUsage() == null) {
            return null;
        }

        var usage = metadata.getUsage();
        return new TokenUsage(
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                usage.getTotalTokens()
        );
    }
}