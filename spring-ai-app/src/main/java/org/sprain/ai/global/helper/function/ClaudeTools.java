package org.sprain.ai.global.helper.function;

import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.global.helper.function.dto.calulator.CalculatorRequest;
import org.sprain.ai.global.helper.function.dto.calulator.CalculatorResponse;
import org.sprain.ai.global.helper.function.dto.common.CurrentTimeResponse;
import org.sprain.ai.global.helper.function.dto.query.UserQueryRequest;
import org.sprain.ai.global.helper.function.dto.query.UserQueryResponse;
import org.sprain.ai.global.helper.function.dto.weather.WeatherRequest;
import org.sprain.ai.global.helper.function.dto.weather.WeatherResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Slf4j
@Component
public class ClaudeTools {

    @Tool(description = "특정 도시의 현재 날씨 정보를 조회합니다")
    public WeatherResponse getWeather(WeatherRequest request) {
        log.info("날씨 조회: {}", request.city());

        Random random = new Random();
        int temperature = 10 + random.nextInt(20);
        String[] conditions = {"맑음", "흐림", "비", "눈"};
        String condition = conditions[random.nextInt(conditions.length)];

        return new WeatherResponse(
            request.city(),
            temperature,
            condition,
            LocalDateTime.now()
        );
    }

    @Tool(description = "두 숫자의 사칙연산을 수행합니다")
    public CalculatorResponse calculator(CalculatorRequest request) {
        log.info("계산: {} {} {}", request.a(), request.operation(), request.b());

        double result = switch (request.operation()) {
            case "add" -> request.a() + request.b();
            case "subtract" -> request.a() - request.b();
            case "multiply" -> request.a() * request.b();
            case "divide" -> {
                if (request.b() == 0) {
                    throw new IllegalArgumentException("0으로 나눌 수 없습니다");
                }
                yield request.a() / request.b();
            }
            default -> throw new IllegalArgumentException("지원하지 않는 연산: " + request.operation());
        };

        return new CalculatorResponse(result);
    }

    @Tool(description = "사용자 정보를 데이터베이스에서 조회합니다")
    public UserQueryResponse getUserInfo(UserQueryRequest request) {
        log.info("사용자 조회: {}", request.userId());

        return new UserQueryResponse(
            request.userId(),
            "홍길동",
            "hong@example.com",
            "010-1234-5678"
        );
    }

    @Tool(description = "현재 날짜와 시간을 반환합니다")
    public CurrentTimeResponse getCurrentTime() {
        log.info("현재 시간 조회");

        LocalDateTime now = LocalDateTime.now();
        return new CurrentTimeResponse(
            now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            now.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분"))
        );
    }
}