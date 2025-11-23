package org.spring.ai.tool.weather;

import lombok.extern.slf4j.Slf4j;
import org.spring.ai.tool.weather.dto.WeatherResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class WeatherDataParser {

    /**
     * 단일 데이터 라인 파싱 (전체 필드)
     */
    public static WeatherResponse parse(String response) {
        try {
            // #START7777과 #7777END 확인
            if (!response.contains("#START7777") && !response.contains("#7777END")) {
                log.warn("Response missing start/end markers");
            }

            // 라인별로 분리
            String[] lines = response.split("\n");

            // 숫자로 시작하는 데이터 라인 찾기
            String dataLine = null;
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() &&
                        !trimmed.startsWith("#") &&
                        Character.isDigit(trimmed.charAt(0))) {
                    dataLine = trimmed;
                    break;
                }
            }

            if (dataLine == null) {
                log.warn("No data line found in response");
                return null;
            }

            return parseWeatherLine(dataLine);

        } catch (Exception e) {
            log.error("Error parsing weather data", e);
            return null;
        }
    }

    /**
     * 여러 데이터 라인 파싱
     */
    public static List<WeatherResponse> parseMultipleLines(String response) {
        List<WeatherResponse> dataList = new ArrayList<>();

        try {
            String[] lines = response.split("\n");

            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() &&
                        !trimmed.startsWith("#") &&
                        Character.isDigit(trimmed.charAt(0))) {
                    WeatherResponse data = parseWeatherLine(trimmed);
                    if (data != null) {
                        dataList.add(data);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing multiple lines", e);
        }

        return dataList;
    }

    /**
     * 단일 라인을 WeatherData로 파싱
     * 포맷: YYMMDDHHMI STN WD WS GST_WD GST_WS GST_TM PA PS PT PR TA TD HM PV RN RN_DAY RN_JUN RN_INT SD_HR3 SD_DAY SD_TOT WC WP WW CA_TOT CA_MID CH_MIN CT CT_TOP CT_MID CT_LOW VS SS SI ST TS TE_5 TE_10 TE_20 TE_30 ST_SEA WH BF IR IX
     */
    private static WeatherResponse parseWeatherLine(String line) {
        try {
            // 공백 기준으로 분리
            String[] fields = line.trim().split("\\s+");

            if (fields.length < 40) {
                log.warn("Insufficient fields: {} (expected at least 40)", fields.length);
                return null;
            }

            WeatherResponse data = WeatherResponse.builder()
                    // 시간/지점
                    .dateTime(fields[0])                              // 0: YYMMDDHHMI
                    .stationId(fields[1])                             // 1: STN ID

                    // 풍향/풍속
                    .windDirection(parseIntOrNull(fields[2]))         // 2: WD
                    .windSpeed(parseFloatOrNull(fields[3]))           // 3: WS
                    .gustWindDirection(parseStringOrNull(fields[4]))  // 4: GST WD
                    .gustWindSpeed(parseFloatOrNull(fields[5]))       // 5: GST WS
                    .gustWindTime(parseStringOrNull(fields[6]))       // 6: GST TM

                    // 기압
                    .pressure(parseFloatOrNull(fields[7]))            // 7: PA
                    .seaLevelPressure(parseFloatOrNull(fields[8]))    // 8: PS
                    .pressureTendency(parseStringOrNull(fields[9]))   // 9: PT
                    .pressure3h(parseFloatOrNull(fields[10]))         // 10: PR

                    // 온도/습도
                    .temperature(parseFloatOrNull(fields[11]))        // 11: TA
                    .dewPoint(parseFloatOrNull(fields[12]))           // 12: TD
                    .humidity(parseFloatOrNull(fields[13]))           // 13: HM
                    .vaporPressure(parseFloatOrNull(fields[14]))      // 14: PV

                    // 강수량
                    .rainfall(parseFloatOrNull(fields[15]))           // 15: RN
                    .rainfallDay(parseFloatOrNull(fields[16]))        // 16: RN DAY
                    .rainfallMonth(parseFloatOrNull(fields[17]))      // 17: RN JUN
                    .rainfallIntensity(parseFloatOrNull(fields[18]))  // 18: RN INT

                    // 적설
                    .snowDepth3h(parseFloatOrNull(fields[19]))        // 19: SD HR3
                    .snowDepthDay(parseFloatOrNull(fields[20]))       // 20: SD DAY
                    .snowDepthTotal(parseFloatOrNull(fields[21]))     // 21: SD TOT

                    // 날씨 현상
                    .weatherCode(parseStringOrNull(fields[22]))       // 22: WC
                    .weatherPhenomena(parseStringOrNull(fields[23]))  // 23: WP
                    .weatherDescription(parseStringOrNull(fields[24]))// 24: WW

                    // 운량
                    .cloudTotal(parseIntOrNull(fields[25]))           // 25: CA TOT
                    .cloudMiddle(parseIntOrNull(fields[26]))          // 26: CA MID
                    .cloudLow(parseIntOrNull(fields[27]))             // 27: CH MIN
                    .cloudType(parseStringOrNull(fields[28]))         // 28: CT
                    .cloudTopHeight(parseIntOrNull(fields[29]))       // 29: CT TOP
                    .cloudMiddleHeight(parseIntOrNull(fields[30]))    // 30: CT MID
                    .cloudLowHeight(parseIntOrNull(fields[31]))       // 31: CT LOW

                    // 시정/일사
                    .visibility(parseIntOrNull(fields[32]))           // 32: VS
                    .sunshine(parseFloatOrNull(fields[33]))           // 33: SS
                    .solarRadiation(parseFloatOrNull(fields[34]))     // 34: SI
                    .snowType(parseStringOrNull(fields[35]))          // 35: ST

                    // 지면/지중온도
                    .groundTemp(parseFloatOrNull(fields[36]))         // 36: TS
                    .soilTemp5cm(parseFloatOrNull(fields[37]))        // 37: TE 5cm
                    .soilTemp10cm(parseFloatOrNull(fields[38]))       // 38: TE 10cm
                    .soilTemp20cm(parseFloatOrNull(fields[39]))       // 39: TE 20cm
                    .soilTemp30cm(parseFloatOrNull(fields[40]))       // 40: TE 30cm
                    .seaTemp(parseFloatOrNull(fields[41]))            // 41: ST SEA

                    // 기타
                    .waveHeight(parseFloatOrNull(fields[42]))         // 42: WH
                    .batteryFlag(parseStringOrNull(fields[43]))       // 43: BF
                    .iceReading(parseStringOrNull(fields[44]))        // 44: IR
                    .iceThickness(parseStringOrNull(fields[45]))      // 45: IX

                    .hasValidData(true)
                    .rawLine(line)
                    .build();

            // 날짜/시간 파싱
            data.parseDateTime();

            return data;

        } catch (Exception e) {
            log.error("Error parsing weather line: {}", line, e);
            return null;
        }
    }

    /**
     * Float 파싱 (결측값 처리)
     */
    private static Float parseFloatOrNull(String value) {
        if (value == null || value.trim().isEmpty() || value.equals("-")) {
            return null;
        }
        try {
            float parsed = Float.parseFloat(value.trim());
            // -9는 결측값
            if (parsed == -9.0f || parsed == -9f) {
                return null;
            }
            return parsed;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Integer 파싱 (결측값 처리)
     */
    private static Integer parseIntOrNull(String value) {
        if (value == null || value.trim().isEmpty() || value.equals("-")) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            // -9는 결측값
            if (parsed == -9) {
                return null;
            }
            return parsed;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * String 파싱 (결측값 처리)
     */
    private static String parseStringOrNull(String value) {
        if (value == null || value.trim().isEmpty() ||
                value.equals("-9") || value.equals("-")) {
            return null;
        }
        return value.trim();
    }

    /**
     * 풍향을 방위로 변환 (16방위)
     */
    public static String getWindDirectionName(Integer direction) {
        if (direction == null) return "정보없음";

        String[] directions = {
                "N", "NNE", "NE", "ENE",
                "E", "ESE", "SE", "SSE",
                "S", "SSW", "SW", "WSW",
                "W", "WNW", "NW", "NNW"
        };

        if (direction >= 0 && direction < directions.length) {
            return directions[direction];
        }
        return "알수없음";
    }

    /**
     * 날씨 상태를 한글로 변환
     */
    public static String getWeatherDescription(String code) {
        if (code == null || code.equals("-")) return "정보없음";

        return switch (code) {
            case "0" -> "맑음";
            case "1" -> "비";
            case "2" -> "비/눈";
            case "3" -> "눈";
            case "4" -> "소나기";
            default -> "기타";
        };
    }
}