package org.sprain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.dto.ImageAnalysis;
import org.sprain.ai.dto.ImageAnalysisResponseV2;
import org.sprain.ai.dto.ReceiptData;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptAnalysisService {
    private final VisionService visionService;
    private final ObjectMapper objectMapper;


    public ImageAnalysisResponseV2<ReceiptData> processReceipt(MultipartFile imageFile) throws IOException, NoSuchFieldException {

        String prompt = """
                이 영수증을 JSON 형식으로 파싱해주세요.
                
                응답 형식:
                {
                  "storeName": "가게명",
                  "address": "주소",
                  "date": "YYYY-MM-DD",
                  "time": "HH:MM",
                  "items": [
                    {"name": "상품명", "quantity": 1, "price": 10000}
                  ],
                  "subtotal": 10000,
                  "tax": 1000,
                  "total": 11000,
                  "paymentMethod": "카드/현금"
                }
                
                JSON만 출력하세요.
                """;

        Class<ReceiptData> receiptDataClass = ReceiptData.class;
        return visionService.analyzeImage(ImageAnalysis.of(prompt, imageFile), receiptDataClass);
    }

    /**
     * JSON 응답 정제 (강화 버전)
     * - 마크다운 코드 블록 제거 (```json ... ```)
     * - 백틱(`) 제거
     * - 앞뒤 공백 제거
     * - 불필요한 텍스트 제거
     */
    private String cleanJsonResponse(String response) throws NoSuchFieldException {
        if (response == null || response.isEmpty()) {
            throw new NoSuchFieldException("Vision API 응답이 비어있습니다");
        }

        log.debug("정제 전 응답: {}", response);

        // 1. 마크다운 코드 블록 제거
        response = response.replaceAll("```json\\s*", "");
        response = response.replaceAll("```\\s*", "");

        // 2. 모든 백틱 제거
        response = response.replace("`", "");

        // 3. 앞뒤 공백 제거
        response = response.trim();

        // 4. JSON 시작 위치 찾기
        int jsonStart = response.indexOf("{");
        int jsonEnd = response.lastIndexOf("}");

        if (jsonStart == -1 || jsonEnd == -1 || jsonStart >= jsonEnd) {
            log.error("유효한 JSON을 찾을 수 없습니다. 응답: {}", response);
            throw new NoSuchFieldException("Vision API 응답에서 JSON을 찾을 수 없습니다");
        }

        // 5. JSON 부분만 추출
        response = response.substring(jsonStart, jsonEnd + 1);

        log.debug("정제 후 응답: {}", response);

        return response;
    }
}
