package org.sprain.ai.external;

import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.external.dto.WeatherData;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class WeatherDataParser {
    private static final String TAG = "WeatherDataParser";

    /**
     * 단일 데이터 라인 파싱
     */
    public static WeatherData parse(String response) {
        try {
            // #START7777과 #7777END 확인
            if (!response.contains("#START7777") || !response.contains("#7777END")) {
                log.info("Invalid response format");
                return null;
            }

            // 라인별로 분리
            String[] lines = response.split("\n");

            // 숫자로 시작하는 데이터 라인 찾기
            String dataLine = null;
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && Character.isDigit(trimmed.charAt(0))) {
                    dataLine = trimmed;
                    break;
                }
            }

            if (dataLine == null) {
                return null;
            }

            // 공백 기준으로 분리
            String[] fields = dataLine.trim().split("\\s+");

            if (fields.length < 13) {
                return null;
            }

            // 데이터 추출
            String dateTime = fields[0];           // 202511112300
            String stationId = fields[1];          // 108
            String windDirection = fields[2];      // 5
            Float windSpeed = parseFloatOrNull(fields[3]);      // 1.1
            Float pressure = parseFloatOrNull(fields[7]);       // 1014.6
            Float temperature = parseFloatOrNull(fields[10]);   // 8.7
            Float dewPoint = parseFloatOrNull(fields[11]);      // 4.5
            Float humidity = parseFloatOrNull(fields[12]);      // 75.0

            return new WeatherData(
                dateTime,
                stationId,
                windDirection,
                windSpeed,
                temperature,
                dewPoint,
                humidity,
                pressure
            );

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 여러 데이터 라인 파싱
     */
    public static List<WeatherData> parseMultipleLines(String response) {
        List<WeatherData> dataList = new ArrayList<>();

        try {
            String[] lines = response.split("\n");

            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && Character.isDigit(trimmed.charAt(0))) {
                    WeatherData data = parseWeatherLine(trimmed);
                    if (data != null) {
                        dataList.add(data);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Parse multiple lines error");
        }

        return dataList;
    }

    /**
     * 단일 라인 파싱
     */
    private static WeatherData parseWeatherLine(String line) {
        try {
            String[] fields = line.trim().split("\\s+");

            if (fields.length < 13) {
                return null;
            }

            return new WeatherData(
                fields[0],
                fields[1],
                fields[2],
                parseFloatOrNull(fields[3]),
                parseFloatOrNull(fields[10]),
                parseFloatOrNull(fields[11]),
                parseFloatOrNull(fields[12]),
                parseFloatOrNull(fields[7])
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Float 파싱 (-9 결측값 처리)
     */
    private static Float parseFloatOrNull(String value) {
        try {
            float parsed = Float.parseFloat(value);
            // -9는 결측값으로 null 반환
            if (parsed == -9.0f || parsed == -9f) {
                return null;
            }
            return parsed;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}