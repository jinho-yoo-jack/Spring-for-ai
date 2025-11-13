package org.sprain.ai.config.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.ai.ollama.OllamaChatModel;
import org.sprain.ai.tools.weather.dto.WeatherRequest;
import org.sprain.ai.tools.weather.dto.WeatherResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Slf4j
@Configuration
public class ChatClientConfig {

    /**
     * ✅ 방법 1: FunctionCallback으로 변환 (권장)
     */
    @Bean(name = "mcpChatClient")
    public ChatClient chatClient(
//            AnthropicChatModel chatModel,
            OllamaChatModel chatModel,
            Function<WeatherRequest, WeatherResponse> getCurrentWeather
    ) {
        log.info("ChatClient 초기화 - Tool 등록");

        // Function을 FunctionCallback으로 래핑
        FunctionCallback weatherCallback = FunctionCallbackWrapper.builder(getCurrentWeather)
                .withName("getCurrentWeather")
                .withDescription("특정 도시의 현재 날씨 정보를 조회합니다. " +
                        "도시 이름(Seoul, Busan 등)을 입력하면 해당 지역의 기온, 습도, 풍속, 기압 등 상세한 날씨 정보를 제공합니다.")
                .withInputType(WeatherRequest.class)
                .build();

        log.info("FunctionCallback 생성 완료: {}", weatherCallback);

        return ChatClient.builder(chatModel)
                .defaultFunctions(weatherCallback)  // ✅ FunctionCallback으로 전달
                .build();
    }
}