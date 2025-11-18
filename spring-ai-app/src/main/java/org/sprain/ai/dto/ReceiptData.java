package org.sprain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 영수증 데이터
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptData {
    private String storeName;
    private LocalDate date;
    private BigDecimal totalAmount;
    private List<ReceiptItem> items;
    private String rawText;  // 원본 텍스트 (파싱 실패 시 참고용)

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiptItem {
        private String name;
        private BigDecimal price;
        private Integer quantity;
    }
}