package com.weatheraqi.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class AqicnApiConfig {
    
    @Value("${aqicn.api.token}")
    private String token;
    
    @Value("${aqicn.api.base-url}")
    private String baseUrl;
}
