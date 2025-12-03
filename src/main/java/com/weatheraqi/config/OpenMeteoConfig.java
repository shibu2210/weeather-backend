package com.weatheraqi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "openmeteo")
@Data
public class OpenMeteoConfig {
    private String baseUrl = "https://api.open-meteo.com/v1";
    private String geocodingUrl = "https://geocoding-api.open-meteo.com/v1";
}
