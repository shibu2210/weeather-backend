package com.weatheraqi.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class WeatherApiConfig {
    
    @Value("${weather.api.key}")
    private String apiKey;
    
    @Value("${weather.api.base-url}")
    private String baseUrl;
}
