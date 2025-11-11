package org.sprain.ai.service;

import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.external.WeatherAPIs;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {
    private final WeatherAPIs weatherAPIs;

    public String getWeather() throws IOException {
        String authKey = "FAVWlSB7Qa6FVpUgezGuHw";
        int stn = 108;
        return weatherAPIs.getWeather(authKey, stn).execute().body();
    }
}
