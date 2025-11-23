package org.spring.ai.tool.weather;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.ai.tool.weather.dto.WeatherRequest;
import org.spring.ai.tool.weather.dto.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import retrofit2.Call;

import java.io.IOException;
import java.util.function.Function;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WeatherTools {

    private final WeatherAPIs weatherAPIs;

    // ✅ application.yml에서 주입받도록 수정
    @Value("${external.api.weather.api-key}")
    private String apiKey;

    @PostConstruct
    public void init(){
        log.info("[API-KEY][Weather] {}", apiKey);
    }

    @Bean(name = "getCurrentWeather")
    @Description("특정 도시의 현재 날씨 정보를 조회합니다. 도시 이름(Seoul, Busan 등)을 입력하면 해당 지역의 기온, 습도, 풍속, 기압 등 상세한 날씨 정보를 제공합니다.")
    public Function<WeatherRequest, WeatherResponse> getCurrentWeather() {
        return request -> {
            try {
                // ✅ 입력 검증 추가
                if (request.city() == null || request.city().isBlank()) {
                    throw new IllegalArgumentException("도시 이름은 필수입니다");
                }

                log.info("날씨 조회 요청 - 도시: {}, 단위: {}",
                        request.city(), request.unit());

                // city를 기반으로 station ID 매핑
                int stn = mapCityToStation(request.city());
                log.debug("Station ID 매핑: {} -> {}", request.city(), stn);

                // API 호출
                Call<String> call = weatherAPIs.getWeather(apiKey, stn);
                retrofit2.Response<String> response = call.execute();

                // 응답 검증
                if (!response.isSuccessful()) {
                    log.error("날씨 API 호출 실패 - 상태코드: {}", response.code());
                    throw new RuntimeException(
                            String.format("날씨 API 호출 실패: %d", response.code())
                    );
                }

                if (response.body() == null || response.body().isEmpty()) {
                    log.error("날씨 API 응답 본문이 비어있음");
                    throw new RuntimeException("날씨 데이터를 가져올 수 없습니다");
                }

                // ✅ String → WeatherResponse 파싱 (static 메서드 호출)
                WeatherResponse weatherResponse =
                        WeatherDataParser.parse(response.body());

                // ✅ 파싱 실패 처리
                if (weatherResponse == null) {
                    log.error("날씨 데이터 파싱 실패");
                    throw new RuntimeException("날씨 데이터 파싱에 실패했습니다");
                }

                // ✅ unit 변환 처리
                if ("fahrenheit".equalsIgnoreCase(request.unit())) {
                    convertToFahrenheit(weatherResponse);
                    log.debug("화씨 온도로 변환 완료");
                }

                log.info("날씨 조회 성공 - 도시: {}, 기온: {}°C",
                        request.city(), weatherResponse.getTemperature());

                return weatherResponse;

            } catch (IllegalArgumentException e) {
                log.error("잘못된 입력: {}", e.getMessage());
                throw e;
            } catch (IOException e) {
                log.error("날씨 API 통신 오류", e);
                throw new RuntimeException("날씨 정보 조회 중 네트워크 오류 발생", e);
            } catch (Exception e) {
                log.error("날씨 정보 조회 중 예상치 못한 오류", e);
                throw new RuntimeException("날씨 정보 조회 중 오류 발생: " + e.getMessage(), e);
            }
        };
    }

    /**
     * 도시 이름을 관측소 ID로 매핑
     * @param city 도시 이름 (대소문자 무관)
     * @return 관측소 ID
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
            // ✅ 더 많은 도시 추가 가능
            default -> {
                log.error("지원하지 않는 도시: {}", city);
                throw new IllegalArgumentException(
                        String.format("지원하지 않는 도시입니다: %s " +
                                "(지원 도시: Seoul, Busan, Incheon, Daegu, Gwangju, " +
                                "Daejeon, Ulsan, Suwon, Jeju, Gangneung)", city)
                );
            }
        };
    }

    /**
     * 섭씨를 화씨로 변환
     * @param response 날씨 응답 객체 (직접 수정됨)
     */
    private void convertToFahrenheit(WeatherResponse response) {
        // 기온 변환
        if (response.getTemperature() != null) {
            response.setTemperature(
                    celsiusToFahrenheit(response.getTemperature())
            );
        }

        // 이슬점 온도 변환
        if (response.getDewPoint() != null) {
            response.setDewPoint(
                    celsiusToFahrenheit(response.getDewPoint())
            );
        }

        // 지면 온도 변환
        if (response.getGroundTemp() != null) {
            response.setGroundTemp(
                    celsiusToFahrenheit(response.getGroundTemp())
            );
        }

        // 지중 온도들 변환
        if (response.getSoilTemp5cm() != null) {
            response.setSoilTemp5cm(
                    celsiusToFahrenheit(response.getSoilTemp5cm())
            );
        }
        if (response.getSoilTemp10cm() != null) {
            response.setSoilTemp10cm(
                    celsiusToFahrenheit(response.getSoilTemp10cm())
            );
        }
        if (response.getSoilTemp20cm() != null) {
            response.setSoilTemp20cm(
                    celsiusToFahrenheit(response.getSoilTemp20cm())
            );
        }
        if (response.getSoilTemp30cm() != null) {
            response.setSoilTemp30cm(
                    celsiusToFahrenheit(response.getSoilTemp30cm())
            );
        }

        // 해수면 온도 변환
        if (response.getSeaTemp() != null) {
            response.setSeaTemp(
                    celsiusToFahrenheit(response.getSeaTemp())
            );
        }
    }

    /**
     * 섭씨를 화씨로 변환하는 헬퍼 메서드
     */
    private float celsiusToFahrenheit(float celsius) {
        return celsius * 9.0f / 5.0f + 32.0f;
    }
}