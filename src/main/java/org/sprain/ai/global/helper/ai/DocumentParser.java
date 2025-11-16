package org.sprain.ai.global.helper.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.dto.BusinessCard;
import org.sprain.ai.dto.DefectReport;
import org.sprain.ai.dto.ReceiptData;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 문서 파싱 유틸리티
 */
@Slf4j
public class DocumentParser {

    private static final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    /**
     * 영수증 데이터 파싱
     */
    public static ReceiptData parseReceiptData(String analysisText) {
        try {
            // JSON 형식으로 응답이 왔는지 확인
            String jsonText = extractJsonFromText(analysisText);

            if (jsonText != null) {
                return parseReceiptFromJson(jsonText, analysisText);
            } else {
                // JSON이 아닌 경우 텍스트 파싱
                return parseReceiptFromText(analysisText);
            }
        } catch (Exception e) {
            log.error("영수증 파싱 실패: {}", e.getMessage());
            return ReceiptData.builder()
                .rawText(analysisText)
                .build();
        }
    }

    /**
     * 명함 데이터 파싱
     */
    public static BusinessCard parseBusinessCard(String analysisText) {
        BusinessCard.BusinessCardBuilder builder = BusinessCard.builder()
            .rawText(analysisText);

        try {
            // 이름 추출
            String name = extractField(analysisText, "이름", "name");
            if (name != null) builder.name(name);

            // 회사명 추출
            String company = extractField(analysisText, "회사명", "company");
            if (company != null) builder.company(company);

            // 직책 추출
            String position = extractField(analysisText, "직책", "position", "직위");
            if (position != null) builder.position(position);

            // 전화번호 추출
            String phone = extractPhoneNumber(analysisText);
            if (phone != null) builder.phoneNumber(phone);

            // 이메일 추출
            String email = extractEmail(analysisText);
            if (email != null) builder.email(email);

            // 주소 추출
            String address = extractField(analysisText, "주소", "address");
            if (address != null) builder.address(address);

            // 부서 추출
            String department = extractField(analysisText, "부서", "department");
            if (department != null) builder.department(department);

        } catch (Exception e) {
            log.error("명함 파싱 실패: {}", e.getMessage());
        }

        return builder.build();
    }

    /**
     * 제품 결함 리포트 파싱
     */
    public static DefectReport parseDefectReport(String analysisText) {
        DefectReport.DefectReportBuilder builder = DefectReport.builder()
            .rawAnalysis(analysisText)
            .defects(new ArrayList<>());

        try {
            // 품질 점수 추출 (1-10)
            Integer qualityScore = extractQualityScore(analysisText);
            builder.qualityScore(qualityScore);

            // 결함 목록 추출
            List<DefectReport.Defect> defects = extractDefects(analysisText);
            builder.defects(defects);

            // 심각도 결정
            DefectReport.DefectSeverity severity = determineSeverity(defects, qualityScore);
            builder.severity(severity);

            // 합격 여부 (품질 점수 7점 이상)
            builder.isAcceptable(qualityScore != null && qualityScore >= 7);

            // 전체 평가
            String assessment = extractOverallAssessment(analysisText);
            builder.overallAssessment(assessment);

        } catch (Exception e) {
            log.error("결함 리포트 파싱 실패: {}", e.getMessage());
        }

        return builder.build();
    }

    // === Private Helper Methods ===

    /**
     * 텍스트에서 JSON 추출
     */
    private static String extractJsonFromText(String text) {
        // ```json과 ``` 사이의 JSON 추출
        Pattern jsonPattern = Pattern.compile("```json\\s*([\\s\\S]*?)```");
        Matcher matcher = jsonPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // 중괄호로 시작하는 JSON 추출
        Pattern bracePattern = Pattern.compile("\\{[\\s\\S]*\\}");
        matcher = bracePattern.matcher(text);
        if (matcher.find()) {
            return matcher.group().trim();
        }

        return null;
    }

    /**
     * JSON에서 영수증 파싱
     */
    private static ReceiptData parseReceiptFromJson(String jsonText, String rawText) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(jsonText);

        ReceiptData.ReceiptDataBuilder builder = ReceiptData.builder()
            .rawText(rawText);

        // 가게명
        if (root.has("storeName")) {
            builder.storeName(root.get("storeName").asText());
        }

        // 날짜
        if (root.has("date")) {
            LocalDate date = parseDate(root.get("date").asText());
            builder.date(date);
        }

        // 총액
        if (root.has("totalAmount")) {
            BigDecimal totalAmount = parseMoney(root.get("totalAmount").asText());
            builder.totalAmount(totalAmount);
        }

        // 항목들
        if (root.has("items") && root.get("items").isArray()) {
            List<ReceiptData.ReceiptItem> items = new ArrayList<>();
            for (JsonNode item : root.get("items")) {
                ReceiptData.ReceiptItem receiptItem = ReceiptData.ReceiptItem.builder()
                    .name(item.has("name") ? item.get("name").asText() : null)
                    .price(item.has("price") ? parseMoney(item.get("price").asText()) : null)
                    .quantity(item.has("quantity") ? item.get("quantity").asInt() : 1)
                    .build();
                items.add(receiptItem);
            }
            builder.items(items);
        }

        return builder.build();
    }

    /**
     * 텍스트에서 영수증 파싱
     */
    private static ReceiptData parseReceiptFromText(String text) {
        ReceiptData.ReceiptDataBuilder builder = ReceiptData.builder()
            .rawText(text);

        // 가게명 추출
        String storeName = extractField(text, "가게명", "storeName", "상호");
        if (storeName != null) builder.storeName(storeName);

        // 날짜 추출
        LocalDate date = extractDateFromText(text);
        if (date != null) builder.date(date);

        // 총액 추출
        BigDecimal totalAmount = extractTotalAmount(text);
        if (totalAmount != null) builder.totalAmount(totalAmount);

        return builder.build();
    }

    /**
     * 필드 추출 (여러 키워드 지원)
     */
    private static String extractField(String text, String... keywords) {
        for (String keyword : keywords) {
            Pattern pattern = Pattern.compile(keyword + "\\s*[:：]?\\s*([^\\n]+)");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        }
        return null;
    }

    /**
     * 날짜 파싱
     */
    private static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }

        // 여러 날짜 형식 시도
        String[] formats = {
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "yyyy.MM.dd",
            "yyyyMMdd"
        };

        for (String format : formats) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(format));
            } catch (DateTimeParseException ignored) {
            }
        }

        log.warn("날짜 파싱 실패: {}", dateStr);
        return null;
    }

    /**
     * 텍스트에서 날짜 추출
     */
    private static LocalDate extractDateFromText(String text) {
        // YYYY-MM-DD 형식
        Pattern pattern = Pattern.compile("(\\d{4}[-./]\\d{2}[-./]\\d{2})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return parseDate(matcher.group(1));
        }
        return null;
    }

    /**
     * 금액 파싱
     */
    private static BigDecimal parseMoney(String moneyStr) {
        if (moneyStr == null || moneyStr.isBlank()) {
            return null;
        }

        try {
            // 숫자가 아닌 문자 제거 (쉼표, 원화 기호 등)
            String cleanStr = moneyStr.replaceAll("[^0-9.]", "");
            return new BigDecimal(cleanStr);
        } catch (NumberFormatException e) {
            log.warn("금액 파싱 실패: {}", moneyStr);
            return null;
        }
    }

    /**
     * 총액 추출
     */
    private static BigDecimal extractTotalAmount(String text) {
        Pattern pattern = Pattern.compile("(?:총액|합계|total)\\s*[:：]?\\s*([0-9,]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return parseMoney(matcher.group(1));
        }
        return null;
    }

    /**
     * 전화번호 추출
     */
    private static String extractPhoneNumber(String text) {
        Pattern pattern = Pattern.compile("(\\d{2,3}[-.]?\\d{3,4}[-.]?\\d{4})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 이메일 추출
     */
    private static String extractEmail(String text) {
        Pattern pattern = Pattern.compile("([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 품질 점수 추출
     */
    private static Integer extractQualityScore(String text) {
        Pattern pattern = Pattern.compile("(?:품질|평가|점수)\\s*[:：]?\\s*(\\d+)(?:/10|점)?");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                int score = Integer.parseInt(matcher.group(1));
                return Math.min(Math.max(score, 1), 10); // 1-10 범위로 제한
            } catch (NumberFormatException e) {
                log.warn("점수 파싱 실패: {}", matcher.group(1));
            }
        }
        return 5; // 기본값
    }

    /**
     * 결함 추출
     */
    private static List<DefectReport.Defect> extractDefects(String text) {
        List<DefectReport.Defect> defects = new ArrayList<>();

        // 결함 키워드 매핑
        String[][] defectKeywords = {
            {"긁힘", "스크래치", "scratch"},
            {"찌그러짐", "dent", "움푹"},
            {"변색", "discoloration", "색상"},
            {"균열", "crack", "금"},
            {"얼룩", "stain"},
            {"변형", "deformation"}
        };

        for (String[] keywords : defectKeywords) {
            for (String keyword : keywords) {
                if (text.toLowerCase().contains(keyword.toLowerCase())) {
                    DefectReport.DefectType type = determineDefectType(keyword);
                    defects.add(DefectReport.Defect.builder()
                        .type(type)
                        .description(keyword + " 발견")
                        .severity(DefectReport.DefectSeverity.MINOR)
                        .build());
                    break; // 각 타입당 하나만
                }
            }
        }

        return defects;
    }

    /**
     * 결함 타입 결정
     */
    private static DefectReport.DefectType determineDefectType(String keyword) {
        if (keyword.contains("긁") || keyword.contains("scratch")) {
            return DefectReport.DefectType.SCRATCH;
        } else if (keyword.contains("찌그") || keyword.contains("dent")) {
            return DefectReport.DefectType.DENT;
        } else if (keyword.contains("변색") || keyword.contains("discolor")) {
            return DefectReport.DefectType.DISCOLORATION;
        } else if (keyword.contains("균열") || keyword.contains("crack")) {
            return DefectReport.DefectType.CRACK;
        } else if (keyword.contains("얼룩") || keyword.contains("stain")) {
            return DefectReport.DefectType.STAIN;
        } else if (keyword.contains("변형") || keyword.contains("deform")) {
            return DefectReport.DefectType.DEFORMATION;
        }
        return DefectReport.DefectType.OTHER;
    }

    /**
     * 심각도 결정
     */
    private static DefectReport.DefectSeverity determineSeverity(
        List<DefectReport.Defect> defects, Integer qualityScore) {

        if (defects.isEmpty()) {
            return DefectReport.DefectSeverity.NONE;
        }

        if (qualityScore != null) {
            if (qualityScore <= 3) return DefectReport.DefectSeverity.CRITICAL;
            if (qualityScore <= 6) return DefectReport.DefectSeverity.MAJOR;
        }

        if (defects.size() >= 3) {
            return DefectReport.DefectSeverity.MAJOR;
        }

        return DefectReport.DefectSeverity.MINOR;
    }

    /**
     * 전체 평가 추출
     */
    private static String extractOverallAssessment(String text) {
        // 마지막 문장이나 평가 부분 추출
        String[] lines = text.split("\\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (!line.isEmpty() && line.length() > 10) {
                return line;
            }
        }
        return text.substring(0, Math.min(200, text.length()));
    }
}