package org.spring.ai.weather;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.ai.dto.WeatherResponse;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component  // ⭐ @Component로 Spring Bean 등록
@RequiredArgsConstructor
public class WeatherMcpService {

    private final WeatherAPIs weatherAPIs;

    @Value("${external.api.weather.api-key}")
    private String apiKey;

    /**
     * ✅ @McpTool 어노테이션으로 MCP Tool 자동 등록
     * - 메서드 이름이 자동으로 Tool 이름이 됨
     * - 파라미터가 자동으로 Tool Arguments가 됨
     */
    @McpTool(
        name = "get_current_weather",  // Tool 이름
        description = "특정 도시의 현재 날씨 정보를 조회합니다. " +
            "도시 이름을 입력하면 기온, 습도, 풍속, 기압 등 " +
            "상세한 날씨 정보를 제공합니다."
    )
    public String getCurrentWeather(
        @McpToolParam(
            description = "날씨를 조회할 도시 이름 (예: Seoul, Busan, Jeju 등)",
            required = true  // 필수 파라미터
        ) String city,

        @McpToolParam(
            description = "온도 단위 (celsius 또는 fahrenheit, 기본값: celsius)",
            required = false  // 선택 파라미터
        ) String unit
    ) {
        try {
            log.info("🌤️ 날씨 조회 요청 - 도시: {}, 단위: {}", city, unit);

            // 1. 도시 이름 → 관측소 ID 매핑
            int stn = mapCityToStation(city);

            // 2. 기본값 설정
            String tempUnit = (unit == null || unit.isEmpty())
                ? "celsius" : unit.toLowerCase();

            // 3. 기상청 API 호출
            var response = weatherAPIs.getWeather(apiKey, stn).execute();

            if (!response.isSuccessful() || response.body() == null) {
                throw new RuntimeException("날씨 API 호출 실패");
            }

            // 4. 데이터 파싱
            WeatherResponse weatherResponse =
                WeatherDataParser.parse(response.body());

            if (weatherResponse == null) {
                throw new RuntimeException("날씨 데이터 파싱 실패");
            }

            // 5. 화씨 변환 (필요시)
            if ("fahrenheit".equalsIgnoreCase(tempUnit)) {
                convertToFahrenheit(weatherResponse);
            }

            // 6. 포맷팅된 문자열 반환
            return formatWeatherResponse(weatherResponse, city, tempUnit);

        } catch (IOException e) {
            log.error("❌ 날씨 조회 실패", e);
            return String.format("날씨 정보를 가져올 수 없습니다: %s", e.getMessage());
        }
    }

    /**
     * 도시 이름 → 관측소 ID 매핑
     */
    private int mapCityToStation(String city) {
        return switch (city.toLowerCase().trim()) {
            case "seoul", "서울" -> 108;
            case "busan", "부산" -> 159;
            case "incheon", "인천" -> 112;
            case "daegu", "대구" -> 143;
            case "gwangju", "광주" -> 156;
            case "daejeon", "대전" -> 133;
            case "ulsan", "울산" -> 152;
            case "suwon", "수원" -> 119;
            case "jeju", "제주" -> 184;
            case "gangneung", "강릉" -> 105;
            default -> throw new IllegalArgumentException(
                String.format("지원하지 않는 도시입니다: %s", city)
            );
        };
    }

    /**
     * 섭씨 → 화씨 변환
     */
    private void convertToFahrenheit(WeatherResponse response) {
        if (response.getTemperature() != null) {
            response.setTemperature(
                celsiusToFahrenheit(response.getTemperature())
            );
        }
        // ... 다른 온도 필드들도 변환
    }

    private float celsiusToFahrenheit(float celsius) {
        return celsius * 9.0f / 5.0f + 32.0f;
    }

    /**
     * 날씨 응답을 읽기 쉬운 문자열로 포맷팅
     */
    private String formatWeatherResponse(
        WeatherResponse response,
        String city,
        String unit) {

        String tempUnit = "celsius".equalsIgnoreCase(unit) ? "°C" : "°F";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📍 **%s 날씨 정보**\n\n", city));
        sb.append(String.format("🌡️ 기온: %.1f%s\n",
            response.getTemperature(), tempUnit));
        sb.append(String.format("💧 습도: %.0f%%\n",
            response.getHumidity()));
        sb.append(String.format("💨 풍속: %.1f m/s\n",
            response.getWindSpeed()));
        sb.append(String.format("🎈 기압: %.1f hPa\n",
            response.getPressure()));

        return sb.toString();
    }
}