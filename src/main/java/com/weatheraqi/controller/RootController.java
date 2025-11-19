package com.weatheraqi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RootController {
    
    @GetMapping("/")
    public Map<String, Object> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Weather AQI API");
        response.put("version", "1.0.0");
        response.put("status", "running");
        response.put("endpoints", Map.of(
            "health", "/api/weather/health",
            "current", "/api/weather/current?location={location}",
            "forecast", "/api/weather/forecast?location={location}&days={days}",
            "search", "/api/weather/search?query={query}"
        ));
        response.put("documentation", "https://github.com/your-repo");
        return response;
    }
    
    @GetMapping("/health")
    public Map<String, String> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Weather AQI API is running");
        return response;
    }
}
