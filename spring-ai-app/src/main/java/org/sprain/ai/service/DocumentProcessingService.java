package org.sprain.ai.service;

import lombok.RequiredArgsConstructor;
import org.sprain.ai.dto.ImageAnalysisResponse;
import org.sprain.ai.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.sprain.ai.global.helper.ai.DocumentParser.*;

@Service
@RequiredArgsConstructor
public class DocumentProcessingService {

    private final VisionService visionService;

    /**
     * 영수증 정보 추출
     */
    public ImageAnalysisResponseV2<ReceiptData> extractReceipt(MultipartFile receiptImage) throws IOException {
        String prompt = """
                이 영수증에서 다음 정보를 JSON 형식으로 추출해주세요:
                {
                  "storeName": "가게명",
                  "date": "날짜",
                  "totalAmount": "총액",
                  "items": [
                    {"name": "상품명", "price": "가격"}
                  ]
                }
                """;

        return visionService.analyzeImage(ImageAnalysis.of(prompt, receiptImage), ReceiptData.class);
    }

    /**
     * 명함 정보 추출
     */
    public ImageAnalysisResponseV2<BusinessCard> extractBusinessCard(MultipartFile cardImage) throws IOException {
        String prompt = """
                이 명함에서 다음 정보를 추출해주세요:
                - 이름
                - 회사명
                - 직책
                - 전화번호
                - 이메일
                - 주소
                """;

        return visionService.analyzeImage(ImageAnalysis.of(prompt, cardImage), BusinessCard.class);
    }

    /**
     * 제품 결함 검사
     */
    public ImageAnalysisResponseV2<DefectReport> inspectProduct(MultipartFile productImage) throws IOException {
        String prompt = """
                이 제품 이미지를 검사하여 다음을 확인해주세요:
                1. 육안으로 보이는 결함
                2. 긁힘, 찌그러짐 등의 손상
                3. 색상 이상
                4. 전체적인 품질 평가 (1-10점)
                """;

        return visionService.analyzeImage(ImageAnalysis.of(prompt, productImage), DefectReport.class);
    }
}
