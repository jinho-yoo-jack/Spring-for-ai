
package org.sprain.ai.external.mcp.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {
    // 시간 정보
    private String dateTime;              // YYMMDDHHMI (202511130800)
    private LocalDateTime parsedDateTime;

    // 지점 정보
    private String stationId;             // STN ID (108)

    // 풍향/풍속
    private Integer windDirection;        // WD 16방위 (5)
    private Float windSpeed;              // WS m/s (2.6)
    private String gustWindDirection;     // GST WD
    private Float gustWindSpeed;          // GST WS
    private String gustWindTime;          // GST TM

    // 기압
    private Float pressure;               // PA 현지기압 hPa (1010.5)
    private Float seaLevelPressure;       // PS 해면기압 hPa (1021.0)
    private String pressureTendency;      // PT 기압경향
    private Float pressure3h;             // PR 3시간 기압변화 hPa

    // 온도/습도
    private Float temperature;            // TA 기온 °C (8.1)
    private Float dewPoint;               // TD 이슬점온도 °C (5.0)
    private Float humidity;               // HM 상대습도 % (81.0)
    private Float vaporPressure;          // PV 수증기압 hPa (8.7)

    // 강수량
    private Float rainfall;               // RN 강수량 mm
    private Float rainfallDay;            // RN DAY 일강수량
    private Float rainfallMonth;          // RN JUN 월강수량
    private Float rainfallIntensity;      // RN INT 강수강도

    // 적설
    private Float snowDepth3h;            // SD HR3 3시간 신적설
    private Float snowDepthDay;           // SD DAY 일신적설
    private Float snowDepthTotal;         // SD TOT 적설

    // 날씨 현상
    private String weatherCode;           // WC 날씨코드
    private String weatherPhenomena;      // WP 현상번호
    private String weatherDescription;    // WW 현재일기

    // 운량
    private Integer cloudTotal;           // CA TOT 전운량 (9)
    private Integer cloudMiddle;          // CA MID 중하층운량 (9)
    private Integer cloudLow;             // CH MIN 최저운고
    private String cloudType;             // CT 운형 (Sc)
    private Integer cloudTopHeight;       // CT TOP
    private Integer cloudMiddleHeight;    // CT MID
    private Integer cloudLowHeight;       // CT LOW (5)

    // 시정/일사
    private Integer visibility;           // VS 시정 (1074)
    private Float sunshine;               // SS 일조 (0.0)
    private Float solarRadiation;         // SI 일사 (0.04)
    private String snowType;              // ST 적설상태

    // 지면/지중온도
    private Float groundTemp;             // TS 지면온도 °C (8.2)
    private Float soilTemp5cm;            // TE 5cm 지중온도 (8.7)
    private Float soilTemp10cm;           // TE 10cm 지중온도 (9.1)
    private Float soilTemp20cm;           // TE 20cm 지중온도 (10.8)
    private Float soilTemp30cm;           // TE 30cm 지중온도 (11.7)
    private Float seaTemp;                // ST SEA 수온

    // 기타
    private Float waveHeight;             // WH 파고 m
    private String batteryFlag;           // BF 배터리 플래그
    private String iceReading;            // IR 결빙
    private String iceThickness;          // IX 결빙두께

    // 데이터 품질
    private boolean hasValidData;
    private String rawLine;

    /**
     * 날짜/시간 파싱 (YYMMDDHHMI -> LocalDateTime)
     */
    public void parseDateTime() {
        if (dateTime != null && dateTime.length() == 12) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
                // YY -> YYYY 변환 (20YY 형태로 가정)
                String fullDateTime = "20" + dateTime;
                this.parsedDateTime = LocalDateTime.parse(fullDateTime, formatter);
            } catch (Exception e) {
                this.parsedDateTime = null;
            }
        }
    }

    /**
     * 결측값 여부 확인
     */
    public boolean isMissingValue(Float value) {
        return value == null || value == -9.0f || value == -9f;
    }

    /**
     * 결측값 여부 확인 (Integer)
     */
    public boolean isMissingValue(Integer value) {
        return value == null || value == -9;
    }

    /**
     * 결측값 여부 확인 (String)
     */
    public boolean isMissingValue(String value) {
        return value == null || value.equals("-9") || value.equals("-");
    }
}