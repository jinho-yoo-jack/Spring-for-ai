package org.sprain.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.dto.ImageAnalysisResponse;
import org.sprain.ai.dto.TokenUsage;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class VisionService {

    private final ChatClient chatClient;

    public VisionService(
        @Qualifier("claudeChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 이미지 분석
     */
    public ImageAnalysisResponse analyzeImage(String prompt, MultipartFile imageFile) {
        try {
            // 1. 이미지 바이트 배열 가져오기
            byte[] imageBytes = imageFile.getBytes();

            // 2. MIME 타입 결정
            String contentType = imageFile.getContentType();
            if (contentType == null) {
                contentType = "image/jpeg"; // 기본값
            }

            // 3. Media 객체 생성
            Media media = new Media(
                MimeTypeUtils.parseMimeType(contentType),
                new ByteArrayResource(imageBytes)
            );

            // 4. UserMessage 생성 (정적 팩토리 메서드 사용)
            UserMessage userMessage = UserMessage.builder()
                .media(List.of(media))
                .text(prompt)
                .build();

            // 5. Claude Vision API 호출
            ChatResponse response = chatClient.prompt()
                .messages(userMessage)
                .call()
                .chatResponse();

            // 6. 응답 추출
            String analysis = response.getResult().getOutput().getText();

            // 7. 토큰 사용량 추출
            TokenUsage tokenUsage = null;
            var metadata = response.getMetadata();
            if (metadata != null && metadata.getUsage() != null) {
                var usage = metadata.getUsage();
                tokenUsage = new TokenUsage(
                    usage.getPromptTokens(),
                    usage.getCompletionTokens(),
                    usage.getTotalTokens()
                );
            }

            return ImageAnalysisResponse.of(
                analysis,
                contentType,
                imageBytes.length,
                tokenUsage
            );

        } catch (IOException e) {
            log.error("이미지 처리 실패: {}", e.getMessage());
            throw new RuntimeException("이미지 처리 중 오류 발생", e);
        }
    }

    /**
     * 이미지에서 텍스트 추출 (OCR)
     */
    public String extractText(MultipartFile imageFile) {
        String prompt = """
            이미지에 있는 모든 텍스트를 정확하게 추출해주세요.
            텍스트만 출력하고, 다른 설명은 필요 없습니다.
            """;

        ImageAnalysisResponse response = analyzeImage(prompt, imageFile);
        return response.analysis();
    }

    /**
     * 이미지 상세 설명 생성
     */
    public String describeImage(MultipartFile imageFile) {
        String prompt = """
            이 이미지를 다음 형식으로 상세히 설명해주세요:
            
            1. 전체적인 장면
            2. 주요 객체들
            3. 색상과 분위기
            4. 특이사항
            """;

        ImageAnalysisResponse response = analyzeImage(prompt, imageFile);
        return response.analysis();
    }

    /**
     * 차트/그래프 분석
     */
    public String analyzeChart(MultipartFile imageFile) {
        String prompt = """
            이 차트/그래프를 분석해주세요:
            
            1. 차트 유형 (막대, 선, 파이 등)
            2. 주요 데이터 포인트
            3. 트렌드와 패턴
            4. 인사이트와 결론
            """;

        ImageAnalysisResponse response = analyzeImage(prompt, imageFile);
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
                    image1.getContentType() != null ? image1.getContentType() : "image/jpeg"
                ),
                new ByteArrayResource(bytes1)
            );

            // 두 번째 이미지 처리
            byte[] bytes2 = image2.getBytes();
            Media media2 = new Media(
                MimeTypeUtils.parseMimeType(
                    image2.getContentType() != null ? image2.getContentType() : "image/jpeg"
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

            // UserMessage 생성 (List로 여러 미디어 전달)
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
            log.error("이미지 비교 실패: {}", e.getMessage());
            throw new RuntimeException("이미지 비교 중 오류 발생", e);
        }
    }
}