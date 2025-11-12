package org.sprain.ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.sprain.ai.external.WeatherAPIs;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.util.concurrent.TimeUnit;

@Configuration
@ConfigurationProperties(prefix = "external.api.weather")
@Setter
@Slf4j
public class WeatherClientConfig {
    private String url;
    private String apiKey;

    @PostConstruct
    public void init(){
        log.info("ApiClient Base URL: {}", url);
        log.info("ApiClient Private Key: {}", apiKey);
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
    }

    @Bean
    public Retrofit retrofit(OkHttpClient okHttpClient) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Retrofit.Builder().baseUrl(url)
            .addConverterFactory(ScalarsConverterFactory.create()) // String 변환용
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .client(okHttpClient)
            .build();
    }

    @Bean
    public WeatherAPIs createApiClient(Retrofit retrofit) {
        return retrofit.create(WeatherAPIs.class);
    }
}
