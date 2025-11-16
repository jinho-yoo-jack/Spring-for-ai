package org.spring.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 날씨 조회 요청 DTO
 */
public record WeatherRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("날씨를 조회할 도시 이름. 지원 도시: Seoul(서울), Busan(부산), Incheon(인천), Daegu(대구), Gwangju(광주), Daejeon(대전), Ulsan(울산), Suwon(수원), Jeju(제주), Gangneung(강릉)")
        String city,

        @JsonProperty(required = false)
        @JsonPropertyDescription("온도 단위. celsius(섭씨, 기본값) 또는 fahrenheit(화씨)")
        String unit
) {
    /**
     * Compact Constructor - 유효성 검증 및 기본값 설정
     */
    public WeatherRequest {
        // ✅ null 체크 추가
        if (city != null) {
            city = city.trim();
        }

        // 기본값 설정
        if (unit == null || unit.isEmpty()) {
            unit = "celsius";
        } else {
            unit = unit.toLowerCase().trim();
            // ✅ 유효한 단위인지 검증
            if (!unit.equals("celsius") && !unit.equals("fahrenheit")) {
                throw new IllegalArgumentException(
                        "온도 단위는 'celsius' 또는 'fahrenheit'만 가능합니다: " + unit
                );
            }
        }
    }

    /**
     * 편의 생성자 - unit 없이 city만으로 생성
     */
    public WeatherRequest(String city) {
        this(city, "celsius");
    }
}