package org.sprain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 제품 결함 리포트
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectReport {
    private DefectSeverity severity;        // 결함 심각도
    private Integer qualityScore;           // 품질 점수 (1-10)
    private List<Defect> defects;          // 발견된 결함 목록
    private String overallAssessment;       // 전체 평가
    private Boolean isAcceptable;           // 합격 여부
    private String rawAnalysis;             // 원본 분석 텍스트

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Defect {
        private DefectType type;
        private String description;
        private String location;
        private DefectSeverity severity;
    }

    public enum DefectType {
        SCRATCH("긁힘"),
        DENT("찌그러짐"),
        DISCOLORATION("변색"),
        CRACK("균열"),
        STAIN("얼룩"),
        DEFORMATION("변형"),
        OTHER("기타");

        private final String description;

        DefectType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum DefectSeverity {
        CRITICAL("심각"),
        MAJOR("주요"),
        MINOR("경미"),
        NONE("없음");

        private final String description;

        DefectSeverity(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}