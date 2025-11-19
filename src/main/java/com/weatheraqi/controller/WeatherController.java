package com.weatheraqi.controller;

import com.weatheraqi.dto.CurrentWeatherResponse;
import com.weatheraqi.dto.ForecastResponse;
import com.weatheraqi.dto.LocationSearchResponse;
import com.weatheraqi.service.WeatherService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Validated
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class WeatherController {
    
    private final WeatherService weatherService;
    
    @GetMapping("/current")
    public ResponseEntity<CurrentWeatherResponse> getCurrentWeather(
            @RequestParam @NotBlank(message = "Location is required") String location) {
        log.info("GET /api/weather/current - location: {}", location);
        CurrentWeatherResponse response = weatherService.getCurrentWeather(location);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/forecast")
    public ResponseEntity<ForecastResponse> getForecast(
            @RequestParam @NotBlank(message = "Location is required") String location,
            @RequestParam(required = false, defaultValue = "3") Integer days) {
        log.info("GET /api/weather/forecast - location: {}, days: {}", location, days);
        ForecastResponse response = weatherService.getForecast(location, days);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    public ResponseEntity<LocationSearchResponse[]> searchLocation(
            @RequestParam @NotBlank(message = "Query is required") String query) {
        log.info("GET /api/weather/search - query: {}", query);
        LocationSearchResponse[] response = weatherService.searchLocation(query);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Weather API is running");
    }
}
