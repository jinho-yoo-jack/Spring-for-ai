package org.sprain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.dto.ImageAnalysis;
import org.sprain.ai.dto.ImageAnalysisResponseV2;
import org.sprain.ai.dto.TokenUsage;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
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

    public static final Class<ChatResponse> DEFAULT_CHAT_RESPONSE_CLASS = ChatResponse.class;
    private static final String DEFAULT_CONTENT_TYPE = "image/jpeg";
    private final ChatClient chatClient;

    public VisionService(@Qualifier("claudeChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 이미지 분석
     */
    @SuppressWarnings("unchecked")
    public <T> ImageAnalysisResponseV2<T> analyzeImage(ImageAnalysis imageAnalysis, Class<T> referenceType) {
        String prompt = imageAnalysis.prompt();

        // 1. 이미지 바이트 배열 가져오기
        byte[] imageBytes = imageAnalysis.imageBytes();

        // 2. MIME 타입 결정
        String contentType = imageAnalysis.contentType();
        if (contentType == null) {
            contentType = DEFAULT_CONTENT_TYPE;
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
        T response = promptWithResponseFormat(userMessage, referenceType);
        TokenUsage tokenUsage = null;

        // 6. 응답 타입에 따라 처리
        if (response instanceof ChatResponse chatResponse) {
            // ChatResponse 타입인 경우
            String analysis = chatResponse.getResult().getOutput().getText();

            // 토큰 사용량 추출
            var metadata = chatResponse.getMetadata();
            if (metadata != null && metadata.getUsage() != null) {
                var usage = metadata.getUsage();
                tokenUsage = new TokenUsage(
                        usage.getPromptTokens(),
                        usage.getCompletionTokens(),
                        usage.getTotalTokens()
                );
            }

            return ImageAnalysisResponseV2.of(
                    (T) analysis,
                    contentType,
                    imageBytes.length,
                    tokenUsage
            );

        } else {
            // String 타입인 경우
            return ImageAnalysisResponseV2.of(
                    response,
                    contentType,
                    imageBytes.length,
                    tokenUsage
            );
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
        ImageAnalysisResponseV2<String> response = analyzeImage(ImageAnalysis.of(prompt, imageFile), String.class);
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

        ImageAnalysisResponseV2<String> response = analyzeImage(ImageAnalysis.of(prompt, imageFile), String.class);
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

        ImageAnalysisResponseV2 response = analyzeImage(ImageAnalysis.of(prompt, imageFile), DEFAULT_CHAT_RESPONSE_CLASS);
        return (String) response.analysis();
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

    private ChatResponse promptByChatResponse(Message userMessage) {
        return chatClient.prompt()
                .messages(userMessage)
                .call()
                .chatResponse();
    }


    @SuppressWarnings("unchecked")
    private <T> T promptWithResponseFormat(Message userMessage, Class<T> responseType) {
        String responseTypeClassName = responseType.getSimpleName();
        if ("ChatResponse".equals(responseTypeClassName)) return (T) promptByChatResponse(userMessage);
        else return chatClient.prompt()
                .messages(userMessage)
                .call()
                .entity(responseType);
    }
}