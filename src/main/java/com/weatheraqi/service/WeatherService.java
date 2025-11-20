package com.weatheraqi.service;

import com.weatheraqi.config.WeatherApiConfig;
import com.weatheraqi.dto.AqiDetailsResponse;
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
    private final AqicnService aqicnService;
    
    @Cacheable(value = "currentWeather", key = "#location")
    public CurrentWeatherResponse getCurrentWeather(String location) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(weatherApiConfig.getBaseUrl() + "/current.json")
                    .queryParam("key", weatherApiConfig.getApiKey())
                    .queryParam("q", location)
                    .queryParam("aqi", "no")
                    .toUriString();
            
            log.info("Fetching current weather for location: {}", location);
            CurrentWeatherResponse response = restTemplate.getForObject(url, CurrentWeatherResponse.class);
            
            if (response == null) {
                throw new WeatherApiException("No data received from Weather API");
            }
            
            // Merge AQICN data
            try {
                mergeAqicnData(response);
            } catch (Exception e) {
                log.warn("Failed to fetch AQICN data, using WeatherAPI AQI: {}", e.getMessage());
            }
            
            return response;
        } catch (Exception e) {
            log.error("Error fetching current weather: {}", e.getMessage());
            throw new WeatherApiException("Failed to fetch weather data: " + e.getMessage());
        }
    }
    
    private void mergeAqicnData(CurrentWeatherResponse weatherResponse) {
        if (weatherResponse.getLocation() == null) {
            return;
        }
        
        try {
            Double lat = weatherResponse.getLocation().getLat();
            Double lon = weatherResponse.getLocation().getLon();
            
            if (lat != null && lon != null) {
                log.info("Fetching AQICN data for coordinates: {}, {}", lat, lon);
                AqiDetailsResponse aqiData = aqicnService.getAqiByCoordinates(lat, lon);
                
                // Replace WeatherAPI AQI with AQICN data
                if (weatherResponse.getCurrent() != null && aqiData != null) {
                    CurrentWeatherResponse.AirQuality airQuality = new CurrentWeatherResponse.AirQuality();
                    
                    // Set the actual AQI value from AQICN
                    airQuality.setAqi(aqiData.getAqi());
                    
                    if (aqiData.getPollutants() != null) {
                        airQuality.setPm2_5(aqiData.getPollutants().getPm25());
                        airQuality.setPm10(aqiData.getPollutants().getPm10());
                        airQuality.setCo(aqiData.getPollutants().getCo());
                        airQuality.setNo2(aqiData.getPollutants().getNo2());
                        airQuality.setSo2(aqiData.getPollutants().getSo2());
                        airQuality.setO3(aqiData.getPollutants().getO3());
                    }
                    
                    // Convert AQICN AQI to EPA index
                    if (aqiData.getAqi() != null) {
                        airQuality.setUsEpaIndex(convertToEpaIndex(aqiData.getAqi()));
                    }
                    
                    weatherResponse.getCurrent().setAirQuality(airQuality);
                    log.info("Successfully merged AQICN data - AQI: {}, Station: {}", aqiData.getAqi(), aqiData.getStationName());
                }
            }
        } catch (Exception e) {
            log.warn("Could not merge AQICN data: {}", e.getMessage());
            throw e;
        }
    }
    
    private Integer convertToEpaIndex(Integer aqicnAqi) {
        // AQICN uses similar scale to EPA, so direct mapping
        if (aqicnAqi <= 50) return 1;      // Good
        if (aqicnAqi <= 100) return 2;     // Moderate
        if (aqicnAqi <= 150) return 3;     // Unhealthy for Sensitive
        if (aqicnAqi <= 200) return 4;     // Unhealthy
        if (aqicnAqi <= 300) return 5;     // Very Unhealthy
        return 6;                           // Hazardous
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
                    .queryParam("aqi", "no")
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
