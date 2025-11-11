package org.sprain.ai.external.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class WeatherData {
    private final String dateTime;      // YYMMDDHHMI
    private final String stationId;     // STN ID
    private final String windDirection; // WD
    private final Float windSpeed;      // WS
    private final Float temperature;    // TA
    private final Float dewPoint;       // TD
    private final Float humidity;       // HM
    private final Float pressure;       // PA

    @Override
    public String toString() {
        return "WeatherData{" +
            "dateTime='" + dateTime + '\'' +
            ", stationId='" + stationId + '\'' +
            ", temperature=" + temperature +
            ", humidity=" + humidity +
            ", pressure=" + pressure +
            '}';
    }
}
