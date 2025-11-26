package org.spring.ai.weather;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spring.ai.tool.weather.WeatherAPIs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WeatherAPIs 실제 API 호출 통합 테스트
 *
 * 이 테스트는 실제 기상청 API를 호출하여 응답을 검증합니다.
 */
@SpringBootTest
@Slf4j
class WeatherAPIsIntegrationTest {

    @Autowired
    private WeatherAPIs weatherAPIs;

    @Value("${external.api.weather.api-key}")
    private String apiKey;

    @Test
    @DisplayName("서울 지점(108) 날씨 조회 - 실제 API 호출 테스트")
    void testGetWeatherForSeoul() throws IOException {
        // Given: 서울 지점 ID (108)
        int seoulStationId = 108;

        log.info("=== 날씨 API 호출 시작 ===");
        log.info("지점 ID: {}", seoulStationId);
        log.info("API Key: {}", apiKey);

        // When: 실제 API 호출
        Call<String> call = weatherAPIs.getWeather(apiKey, seoulStationId);
        Response<String> response = call.execute();

        // Then: 응답 검증
        log.info("응답 상태 코드: {}", response.code());
        assertTrue(response.isSuccessful(), "API 호출이 성공해야 합니다.");
        assertEquals(200, response.code(), "HTTP 상태 코드는 200이어야 합니다.");

        String body = response.body();
        assertNotNull(body, "응답 본문이 null이 아니어야 합니다.");
        assertFalse(body.isEmpty(), "응답 본문이 비어있지 않아야 합니다.");

        // 기상청 API 응답 형식 검증
        assertTrue(body.contains("#"), "응답에 헤더가 포함되어야 합니다.");
        assertTrue(body.contains("END"), "응답에 종료 마커가 포함되어야 합니다.");

        log.info("=== 응답 본문 ===");
        log.info("\n{}", body);
        log.info("=== 날씨 API 호출 완료 ===");
    }

    @Test
    @DisplayName("부산 지점(159) 날씨 조회 - 실제 API 호출 테스트")
    void testGetWeatherForBusan() throws IOException {
        // Given: 부산 지점 ID (159)
        int busanStationId = 159;

        log.info("=== 날씨 API 호출 시작 (부산) ===");
        log.info("지점 ID: {}", busanStationId);

        // When: 실제 API 호출
        Call<String> call = weatherAPIs.getWeather(apiKey, busanStationId);
        Response<String> response = call.execute();

        // Then: 응답 검증
        log.info("응답 상태 코드: {}", response.code());
        assertTrue(response.isSuccessful(), "API 호출이 성공해야 합니다.");

        String body = response.body();
        assertNotNull(body, "응답 본문이 null이 아니어야 합니다.");

        log.info("=== 응답 본문 ===");
        log.info("\n{}", body);
        log.info("=== 날씨 API 호출 완료 (부산) ===");
    }

    @Test
    @DisplayName("인천 지점(112) 날씨 조회 - 실제 API 호출 테스트")
    void testGetWeatherForIncheon() throws IOException {
        // Given: 인천 지점 ID (112)
        int incheonStationId = 112;

        log.info("=== 날씨 API 호출 시작 (인천) ===");
        log.info("지점 ID: {}", incheonStationId);

        // When: 실제 API 호출
        Call<String> call = weatherAPIs.getWeather(apiKey, incheonStationId);
        Response<String> response = call.execute();

        // Then: 응답 검증
        log.info("응답 상태 코드: {}", response.code());
        assertTrue(response.isSuccessful(), "API 호출이 성공해야 합니다.");

        String body = response.body();
        assertNotNull(body, "응답 본문이 null이 아니어야 합니다.");

        log.info("=== 응답 본문 ===");
        log.info("\n{}", body);
        log.info("=== 날씨 API 호출 완료 (인천) ===");
    }

    @Test
    @DisplayName("대구 지점(143) 날씨 조회 - 실제 API 호출 테스트")
    void testGetWeatherForDaegu() throws IOException {
        // Given: 대구 지점 ID (143)
        int daeguStationId = 143;

        log.info("=== 날씨 API 호출 시작 (대구) ===");
        log.info("지점 ID: {}", daeguStationId);

        // When: 실제 API 호출
        Call<String> call = weatherAPIs.getWeather(apiKey, daeguStationId);
        Response<String> response = call.execute();

        // Then: 응답 검증
        log.info("응답 상태 코드: {}", response.code());
        assertTrue(response.isSuccessful(), "API 호출이 성공해야 합니다.");

        String body = response.body();
        assertNotNull(body, "응답 본문이 null이 아니어야 합니다.");

        log.info("=== 응답 본문 ===");
        log.info("\n{}", body);
        log.info("=== 날씨 API 호출 완료 (대구) ===");
    }

    @Test
    @DisplayName("제주 지점(184) 날씨 조회 - 실제 API 호출 테스트")
    void testGetWeatherForJeju() throws IOException {
        // Given: 제주 지점 ID (184)
        int jejuStationId = 184;

        log.info("=== 날씨 API 호출 시작 (제주) ===");
        log.info("지점 ID: {}", jejuStationId);

        // When: 실제 API 호출
        Call<String> call = weatherAPIs.getWeather(apiKey, jejuStationId);
        Response<String> response = call.execute();

        // Then: 응답 검증
        log.info("응답 상태 코드: {}", response.code());
        assertTrue(response.isSuccessful(), "API 호출이 성공해야 합니다.");

        String body = response.body();
        assertNotNull(body, "응답 본문이 null이 아니어야 합니다.");

        log.info("=== 응답 본문 ===");
        log.info("\n{}", body);
        log.info("=== 날씨 API 호출 완료 (제주) ===");
    }

    @Test
    @DisplayName("여러 지점 순차 조회 - 모든 지점이 정상 응답하는지 확인")
    void testMultipleStations() throws IOException {
        // Given: 여러 주요 도시 지점 ID들
        int[] stationIds = {
            108,  // 서울
            112,  // 인천
            143,  // 대구
            156,  // 광주
            159,  // 부산
            133,  // 대전
            152,  // 울산
            184,  // 제주
            105   // 강릉
        };

        String[] cityNames = {"서울", "인천", "대구", "광주", "부산", "대전", "울산", "제주", "강릉"};

        log.info("=== 여러 지점 순차 조회 시작 ===");

        // When & Then: 각 지점별로 API 호출 및 검증
        for (int i = 0; i < stationIds.length; i++) {
            int stationId = stationIds[i];
            String cityName = cityNames[i];

            log.info("\n--- {} (ID: {}) 조회 중... ---", cityName, stationId);

            Call<String> call = weatherAPIs.getWeather(apiKey, stationId);
            Response<String> response = call.execute();

            assertTrue(response.isSuccessful(),
                cityName + " API 호출이 성공해야 합니다.");
            assertNotNull(response.body(),
                cityName + " 응답 본문이 null이 아니어야 합니다.");

            log.info("{} 조회 성공 (응답 길이: {} bytes)",
                cityName, response.body().length());
        }

        log.info("\n=== 모든 지점 조회 완료 ===");
    }

    @Test
    @DisplayName("WeatherAPIs 빈이 정상적으로 주입되는지 확인")
    void testWeatherAPIsBeanInjection() {
        // Then: WeatherAPIs 빈이 정상적으로 주입되었는지 확인
        assertNotNull(weatherAPIs, "WeatherAPIs 빈이 null이 아니어야 합니다.");
        log.info("WeatherAPIs 빈 주입 성공: {}", weatherAPIs.getClass().getName());
    }

    @Test
    @DisplayName("API 키가 정상적으로 설정되어 있는지 확인")
    void testApiKeyConfiguration() {
        // Then: API 키가 설정되어 있는지 확인
        assertNotNull(apiKey, "API 키가 null이 아니어야 합니다.");
        assertFalse(apiKey.isEmpty(), "API 키가 비어있지 않아야 합니다.");
        log.info("API 키 설정 확인 완료: {}", apiKey);
    }
}