package org.sprain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 명함 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessCard {
    private String name;
    private String company;
    private String position;
    private String phoneNumber;
    private String email;
    private String address;
    private String department;  // 부서
    private String faxNumber;   // 팩스
    private String website;     // 웹사이트
    private String rawText;     // 원본 텍스트
}