package org.sprain.ai.controller;

import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.external.WeatherAPIs;
import org.sprain.ai.service.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tools/")
@Slf4j
public class WeatherController {
    private final WeatherService weatherService;

    @GetMapping("/weather")
    public void getWeather() throws IOException {
        log.info("{}",weatherService.getWeather());
    }
}
