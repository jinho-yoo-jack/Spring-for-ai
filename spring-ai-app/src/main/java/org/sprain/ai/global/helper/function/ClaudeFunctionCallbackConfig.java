package org.sprain.ai.global.helper.function;

import lombok.RequiredArgsConstructor;
import org.sprain.ai.global.helper.function.dto.calulator.CalculatorRequest;
import org.sprain.ai.global.helper.function.dto.calulator.CalculatorResponse;
import org.sprain.ai.global.helper.function.dto.common.CurrentTimeResponse;
import org.sprain.ai.global.helper.function.dto.query.UserQueryRequest;
import org.sprain.ai.global.helper.function.dto.query.UserQueryResponse;
import org.sprain.ai.global.helper.function.dto.weather.WeatherRequest;
import org.sprain.ai.global.helper.function.dto.weather.WeatherResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

@Configuration
@RequiredArgsConstructor
public class ClaudeFunctionCallbackConfig {

    /**
     * 날씨 조회 함수
     */
    @Bean
    public ToolCallback getWeatherCallback() {
        Function<WeatherRequest, WeatherResponse> function = request -> {
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
        };

        return FunctionToolCallback.builder("getWeather", function)
            .description("특정 도시의 현재 날씨 정보를 조회합니다")
            .inputType(WeatherRequest.class)
            .build();
    }

    /**
     * 계산기 함수
     */
    @Bean
    public ToolCallback calculatorCallback() {
        Function<CalculatorRequest, CalculatorResponse> function = request -> {
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
        };

        return FunctionToolCallback.builder("calculator", function)
            .description("두 숫자의 사칙연산을 수행합니다")
            .inputType(CalculatorRequest.class)
            .build();
    }

    /**
     * 데이터베이스 조회 함수
     */
    @Bean
    public ToolCallback getUserInfoCallback() {
        Function<UserQueryRequest, UserQueryResponse> function = request -> {
            return new UserQueryResponse(
                request.userId(),
                "홍길동",
                "hong@example.com",
                "010-1234-5678"
            );
        };

        return FunctionToolCallback.builder("getUserInfo", function)
            .description("사용자 정보를 데이터베이스에서 조회합니다")
            .inputType(UserQueryRequest.class)
            .build();
    }

    /**
     * 현재 시간 조회
     */
    @Bean
    public ToolCallback getCurrentTimeCallback() {
        Supplier<CurrentTimeResponse> supplier = () -> {
            LocalDateTime now = LocalDateTime.now();
            return new CurrentTimeResponse(
                now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                now.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분"))
            );
        };

        // Supplier는 FunctionToolCallback으로 변환
        // Supplier를 Function<Void, T>로 래핑
        Function<Void, CurrentTimeResponse> function = (Void v) -> supplier.get();

        return FunctionToolCallback.builder("getCurrentTime", function)
            .description("현재 날짜와 시간을 반환합니다")
            .inputType(Void.class)
            .build();
    }
}