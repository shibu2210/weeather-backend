package com.weatheraqi.service;

import com.weatheraqi.config.OpenMeteoConfig;
import com.weatheraqi.dto.*;
import com.weatheraqi.exception.WeatherApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenMeteoService {
    
    private final RestTemplate restTemplate;
    private final OpenMeteoConfig config;
    
    private static final Map<Integer, String> WEATHER_CODE_MAP = new HashMap<>();
    
    static {
        WEATHER_CODE_MAP.put(0, "Clear sky");
        WEATHER_CODE_MAP.put(1, "Mainly clear");
        WEATHER_CODE_MAP.put(2, "Partly cloudy");
        WEATHER_CODE_MAP.put(3, "Overcast");
        WEATHER_CODE_MAP.put(45, "Foggy");
        WEATHER_CODE_MAP.put(48, "Depositing rime fog");
        WEATHER_CODE_MAP.put(51, "Light drizzle");
        WEATHER_CODE_MAP.put(53, "Moderate drizzle");
        WEATHER_CODE_MAP.put(55, "Dense drizzle");
        WEATHER_CODE_MAP.put(61, "Slight rain");
        WEATHER_CODE_MAP.put(63, "Moderate rain");
        WEATHER_CODE_MAP.put(65, "Heavy rain");
        WEATHER_CODE_MAP.put(71, "Slight snow");
        WEATHER_CODE_MAP.put(73, "Moderate snow");
        WEATHER_CODE_MAP.put(75, "Heavy snow");
        WEATHER_CODE_MAP.put(77, "Snow grains");
        WEATHER_CODE_MAP.put(80, "Slight rain showers");
        WEATHER_CODE_MAP.put(81, "Moderate rain showers");
        WEATHER_CODE_MAP.put(82, "Violent rain showers");
        WEATHER_CODE_MAP.put(85, "Slight snow showers");
        WEATHER_CODE_MAP.put(86, "Heavy snow showers");
        WEATHER_CODE_MAP.put(95, "Thunderstorm");
        WEATHER_CODE_MAP.put(96, "Thunderstorm with slight hail");
        WEATHER_CODE_MAP.put(99, "Thunderstorm with heavy hail");
    }
    
    public GeocodingResponse searchLocation(String query) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(config.getGeocodingUrl() + "/search")
                    .queryParam("name", query)
                    .queryParam("count", 10)
                    .queryParam("language", "en")
                    .queryParam("format", "json")
                    .toUriString();
            
            log.info("Searching locations with Open-Meteo: {}", query);
            GeocodingResponse response = restTemplate.getForObject(url, GeocodingResponse.class);
            
            return response != null ? response : new GeocodingResponse();
        } catch (Exception e) {
            log.error("Error searching locations: {}", e.getMessage());
            throw new WeatherApiException("Failed to search locations: " + e.getMessage());
        }
    }
    
    public OpenMeteoResponse getWeatherData(Double lat, Double lon, Integer forecastDays) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(config.getBaseUrl() + "/forecast")
                    .queryParam("latitude", lat)
                    .queryParam("longitude", lon)
                    .queryParam("current", "temperature_2m,relative_humidity_2m,apparent_temperature,is_day," +
                            "precipitation,weather_code,cloud_cover,pressure_msl,surface_pressure," +
                            "wind_speed_10m,wind_direction_10m,wind_gusts_10m")
                    .queryParam("hourly", "temperature_2m,relative_humidity_2m,apparent_temperature," +
                            "precipitation_probability,precipitation,weather_code,visibility," +
                            "wind_speed_10m,wind_direction_10m,uv_index,is_day")
                    .queryParam("daily", "weather_code,temperature_2m_max,temperature_2m_min," +
                            "apparent_temperature_max,apparent_temperature_min,sunrise,sunset," +
                            "uv_index_max,precipitation_sum,precipitation_probability_max," +
                            "wind_speed_10m_max,wind_gusts_10m_max,wind_direction_10m_dominant")
                    .queryParam("temperature_unit", "celsius")
                    .queryParam("wind_speed_unit", "kmh")
                    .queryParam("precipitation_unit", "mm")
                    .queryParam("timezone", "auto")
                    .queryParam("forecast_days", forecastDays != null ? forecastDays : 7)
                    .toUriString();
            
            log.info("Fetching weather from Open-Meteo: lat={}, lon={}, days={}", lat, lon, forecastDays);
            OpenMeteoResponse response = restTemplate.getForObject(url, OpenMeteoResponse.class);
            
            if (response == null) {
                throw new WeatherApiException("No data received from Open-Meteo");
            }
            
            return response;
        } catch (Exception e) {
            log.error("Error fetching weather data: {}", e.getMessage());
            throw new WeatherApiException("Failed to fetch weather data: " + e.getMessage());
        }
    }
    
    public static String getWeatherDescription(Integer code) {
        return WEATHER_CODE_MAP.getOrDefault(code, "Unknown");
    }
    
    public static String getWeatherIconCode(Integer code, Integer isDay) {
        boolean day = isDay != null && isDay == 1;
        
        if (code == null) return day ? "113" : "116"; // Default clear
        
        switch (code) {
            case 0: return day ? "113" : "113"; // Clear
            case 1: return day ? "116" : "116"; // Mainly clear
            case 2: return day ? "116" : "119"; // Partly cloudy
            case 3: return "122"; // Overcast
            case 45:
            case 48: return "248"; // Fog
            case 51:
            case 53:
            case 55: return "263"; // Drizzle
            case 61: return "293"; // Slight rain
            case 63: return "296"; // Moderate rain
            case 65: return "308"; // Heavy rain
            case 71: return "326"; // Slight snow
            case 73: return "332"; // Moderate snow
            case 75: return "338"; // Heavy snow
            case 77: return "368"; // Snow grains
            case 80: return "353"; // Slight rain showers
            case 81: return "356"; // Moderate rain showers
            case 82: return "359"; // Violent rain showers
            case 85: return "368"; // Slight snow showers
            case 86: return "371"; // Heavy snow showers
            case 95: return "386"; // Thunderstorm
            case 96:
            case 99: return "389"; // Thunderstorm with hail
            default: return day ? "113" : "116";
        }
    }
    
    public static String getWindDirection(Integer degrees) {
        if (degrees == null) return "N";
        
        String[] directions = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                              "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
        int index = (int) Math.round(((degrees % 360) / 22.5));
        return directions[index % 16];
    }
    
    public PrecipitationMinutelyResponse getPrecipitationMinutely(Double lat, Double lon) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(config.getBaseUrl() + "/forecast")
                    .queryParam("latitude", lat)
                    .queryParam("longitude", lon)
                    .queryParam("minutely_15", "precipitation")
                    .queryParam("forecast_days", 1)
                    .queryParam("timezone", "auto")
                    .toUriString();
            
            log.info("Fetching minutely precipitation from Open-Meteo: lat={}, lon={}", lat, lon);
            PrecipitationMinutelyResponse response = restTemplate.getForObject(url, PrecipitationMinutelyResponse.class);
            
            if (response == null) {
                throw new WeatherApiException("No precipitation data received from Open-Meteo");
            }
            
            return response;
        } catch (Exception e) {
            log.error("Error fetching precipitation data: {}", e.getMessage());
            throw new WeatherApiException("Failed to fetch precipitation data: " + e.getMessage());
        }
    }
    
    public PollenForecastResponse getPollenForecast(Double lat, Double lon) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://air-quality-api.open-meteo.com/v1/air-quality")
                    .queryParam("latitude", lat)
                    .queryParam("longitude", lon)
                    .queryParam("daily", "alder_pollen,birch_pollen,grass_pollen,mugwort_pollen,olive_pollen,ragweed_pollen")
                    .queryParam("timezone", "auto")
                    .queryParam("forecast_days", 7)
                    .toUriString();
            
            log.info("Fetching pollen forecast from Open-Meteo: lat={}, lon={}", lat, lon);
            PollenForecastResponse response = restTemplate.getForObject(url, PollenForecastResponse.class);
            
            if (response == null) {
                throw new WeatherApiException("No pollen data received from Open-Meteo");
            }
            
            return response;
        } catch (Exception e) {
            log.error("Error fetching pollen data: {}", e.getMessage());
            throw new WeatherApiException("Failed to fetch pollen data: " + e.getMessage());
        }
    }
}
