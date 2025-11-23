package org.spring.ai.weather;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.spring.ai.tool.weather.WeatherDataParser;
import org.spring.ai.tool.weather.dto.WeatherResponse;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class WeatherResponseParserTest {

    @Test
    void testParseWeatherData() {
        String response = """
                #2345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234
                # YYMMDDHHMI STN  WD   WS GST  GST  GST     PA     PS PT    PR    TA    TD    HM    PV     RN     RN     RN     RN     SD     SD     SD WC WP WW                      CA  CA   CH CT        CT  CT  CT    VS   SS    SI ST    TS    TE    TE    TE    TE  ST   WH BF IR IX
                #        KST  ID  16  m/s  WD   WS   TM    hPa    hPa  -   hPa     C     C     %   hPa     mm    DAY    JUN    INT    HR3    DAY    TOT -- -- ---------------------- TOT MID  MIN -------- TOP MID LOW                  GD     C     5    10    20    30 SEA    m --  -  -
                202511130800 108   5  2.6  -9 -9.0   -9 1010.5 1021.0 -9  -9.0   8.1   5.0  81.0   8.7   -9.0   -9.0   -9.0   -9.0   -9.0   -9.0   -9.0 -9 -9 -                        9   9   10 Sc         0   0   5  1074  0.0  0.04 -9   8.2   8.7   9.1  10.8  11.7  -9 -9.0 -9  3  2
                #7777END
                """;

        WeatherResponse data = WeatherDataParser.parse(response);

        assertNotNull(data);
        assertEquals("202511130800", data.getDateTime());
        assertEquals("108", data.getStationId());
        assertEquals(5, data.getWindDirection());
        assertEquals(2.6f, data.getWindSpeed(), 0.01f);
        assertEquals(1010.5f, data.getPressure(), 0.01f);
        assertEquals(1021.0f, data.getSeaLevelPressure(), 0.01f);
        assertEquals(8.1f, data.getTemperature(), 0.01f);
        assertEquals(5.0f, data.getDewPoint(), 0.01f);
        assertEquals(81.0f, data.getHumidity(), 0.01f);
        assertEquals(8.7f, data.getVaporPressure(), 0.01f);
        assertEquals(9, data.getCloudTotal());
        assertEquals(9, data.getCloudMiddle());
        assertEquals(10, data.getCloudLow());
        assertEquals("Sc", data.getCloudType());
        assertEquals(5, data.getCloudLowHeight());
        assertEquals(1074, data.getVisibility());
        assertEquals(0.0f, data.getSunshine(), 0.01f);
        assertEquals(0.04f, data.getSolarRadiation(), 0.01f);
        assertEquals(8.2f, data.getGroundTemp(), 0.01f);
        assertEquals(8.7f, data.getSoilTemp5cm(), 0.01f);
        assertEquals(9.1f, data.getSoilTemp10cm(), 0.01f);
        assertEquals(10.8f, data.getSoilTemp20cm(), 0.01f);
        assertEquals(11.7f, data.getSoilTemp30cm(), 0.01f);

        // 결측값 확인
        assertNull(data.getGustWindSpeed());
        assertNull(data.getPressure3h());
        assertNull(data.getRainfall());

        log.info("Parsed data: {}", data);
        log.info("Wind direction: {}", WeatherDataParser.getWindDirectionName(data.getWindDirection()));
        log.info("DateTime: {}", data.getParsedDateTime());
    }
}