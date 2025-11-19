package com.weatheraqi.service;

import com.weatheraqi.config.WeatherApiConfig;
import com.weatheraqi.dto.CurrentWeatherResponse;
import com.weatheraqi.dto.ForecastResponse;
import com.weatheraqi.dto.LocationSearchResponse;
import com.weatheraqi.exception.WeatherApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {
    
    private final RestTemplate restTemplate;
    private final WeatherApiConfig weatherApiConfig;
    
    @Cacheable(value = "currentWeather", key = "#location")
    public CurrentWeatherResponse getCurrentWeather(String location) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(weatherApiConfig.getBaseUrl() + "/current.json")
                    .queryParam("key", weatherApiConfig.getApiKey())
                    .queryParam("q", location)
                    .queryParam("aqi", "yes")
                    .toUriString();
            
            log.info("Fetching current weather for location: {}", location);
            CurrentWeatherResponse response = restTemplate.getForObject(url, CurrentWeatherResponse.class);
            
            if (response == null) {
                throw new WeatherApiException("No data received from Weather API");
            }
            
            return response;
        } catch (Exception e) {
            log.error("Error fetching current weather: {}", e.getMessage());
            throw new WeatherApiException("Failed to fetch weather data: " + e.getMessage());
        }
    }
    
    @Cacheable(value = "forecast", key = "#location + '_' + #days")
    public ForecastResponse getForecast(String location, Integer days) {
        try {
            if (days == null || days < 1) {
                days = 3;
            }
            if (days > 10) {
                days = 10;
            }
            
            String url = UriComponentsBuilder
                    .fromHttpUrl(weatherApiConfig.getBaseUrl() + "/forecast.json")
                    .queryParam("key", weatherApiConfig.getApiKey())
                    .queryParam("q", location)
                    .queryParam("days", days)
                    .queryParam("aqi", "yes")
                    .queryParam("alerts", "no")
                    .toUriString();
            
            log.info("Fetching forecast for location: {}, days: {}", location, days);
            ForecastResponse response = restTemplate.getForObject(url, ForecastResponse.class);
            
            if (response == null) {
                throw new WeatherApiException("No forecast data received from Weather API");
            }
            
            return response;
        } catch (Exception e) {
            log.error("Error fetching forecast: {}", e.getMessage());
            throw new WeatherApiException("Failed to fetch forecast data: " + e.getMessage());
        }
    }
    
    @Cacheable(value = "locationSearch", key = "#query")
    public LocationSearchResponse[] searchLocation(String query) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(weatherApiConfig.getBaseUrl() + "/search.json")
                    .queryParam("key", weatherApiConfig.getApiKey())
                    .queryParam("q", query)
                    .toUriString();
            
            log.info("Searching locations for query: {}", query);
            LocationSearchResponse[] response = restTemplate.getForObject(url, LocationSearchResponse[].class);
            
            return response != null ? response : new LocationSearchResponse[0];
        } catch (Exception e) {
            log.error("Error searching locations: {}", e.getMessage());
            throw new WeatherApiException("Failed to search locations: " + e.getMessage());
        }
    }
}
