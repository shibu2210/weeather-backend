package com.weatheraqi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class WeatherAqiApplication {
    public static void main(String[] args) {
        SpringApplication.run(WeatherAqiApplication.class, args);
    }
}
