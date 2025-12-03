package com.weatheraqi.service;

import com.weatheraqi.dto.*;
import com.weatheraqi.exception.WeatherApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {
    
    private final OpenMeteoService openMeteoService;
    private final AqicnService aqicnService;
    private final HealthInsightsService healthInsightsService;
    
    @Cacheable(value = "currentWeather", key = "#location")
    public CurrentWeatherResponse getCurrentWeather(String location) {
        try {
            log.info("Fetching current weather for location: {}", location);
            
            // Parse location - could be "city name" or "lat,lon"
            Double lat, lon;
            String locationName;
            
            if (location.contains(",")) {
                String[] parts = location.split(",");
                lat = Double.parseDouble(parts[0].trim());
                lon = Double.parseDouble(parts[1].trim());
                locationName = "Location";
            } else {
                // Search for location first
                GeocodingResponse geocoding = openMeteoService.searchLocation(location);
                if (geocoding.getResults() == null || geocoding.getResults().isEmpty()) {
                    throw new WeatherApiException("Location not found: " + location);
                }
                GeocodingResponse.Result firstResult = geocoding.getResults().get(0);
                lat = firstResult.getLatitude();
                lon = firstResult.getLongitude();
                locationName = firstResult.getName();
            }
            
            // Fetch weather data
            OpenMeteoResponse omResponse = openMeteoService.getWeatherData(lat, lon, 1);
            
            // Transform to CurrentWeatherResponse
            CurrentWeatherResponse response = transformToCurrentWeather(omResponse, locationName);
            
            // Merge AQICN data
            try {
                mergeAqicnData(response);
            } catch (Exception e) {
                log.warn("Failed to fetch AQICN data: {}", e.getMessage());
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
                days = 7;
            }
            if (days > 16) {
                days = 16;
            }
            
            log.info("Fetching forecast for location: {}, days: {}", location, days);
            
            // Parse location
            Double lat, lon;
            String locationName;
            
            if (location.contains(",")) {
                String[] parts = location.split(",");
                lat = Double.parseDouble(parts[0].trim());
                lon = Double.parseDouble(parts[1].trim());
                locationName = "Location";
            } else {
                GeocodingResponse geocoding = openMeteoService.searchLocation(location);
                if (geocoding.getResults() == null || geocoding.getResults().isEmpty()) {
                    throw new WeatherApiException("Location not found: " + location);
                }
                GeocodingResponse.Result firstResult = geocoding.getResults().get(0);
                lat = firstResult.getLatitude();
                lon = firstResult.getLongitude();
                locationName = firstResult.getName();
            }
            
            // Fetch weather data
            OpenMeteoResponse omResponse = openMeteoService.getWeatherData(lat, lon, days);
            
            // Transform to ForecastResponse
            ForecastResponse response = transformToForecast(omResponse, locationName);
            
            return response;
        } catch (Exception e) {
            log.error("Error fetching forecast: {}", e.getMessage());
            throw new WeatherApiException("Failed to fetch forecast data: " + e.getMessage());
        }
    }
    
    @Cacheable(value = "locationSearch", key = "#query")
    public LocationSearchResponse[] searchLocation(String query) {
        try {
            log.info("Searching locations for query: {}", query);
            GeocodingResponse geocoding = openMeteoService.searchLocation(query);
            
            if (geocoding.getResults() == null || geocoding.getResults().isEmpty()) {
                return new LocationSearchResponse[0];
            }
            
            // Transform to LocationSearchResponse array
            return geocoding.getResults().stream()
                    .map(this::transformToLocationSearch)
                    .toArray(LocationSearchResponse[]::new);
        } catch (Exception e) {
            log.error("Error searching locations: {}", e.getMessage());
            throw new WeatherApiException("Failed to search locations: " + e.getMessage());
        }
    }
    
    private LocationSearchResponse transformToLocationSearch(GeocodingResponse.Result result) {
        LocationSearchResponse response = new LocationSearchResponse();
        response.setId(result.getId());
        response.setName(result.getName());
        response.setRegion(result.getAdmin1() != null ? result.getAdmin1() : "");
        response.setCountry(result.getCountry() != null ? result.getCountry() : "");
        response.setLat(result.getLatitude());
        response.setLon(result.getLongitude());
        response.setUrl("");
        return response;
    }
    
    private CurrentWeatherResponse transformToCurrentWeather(OpenMeteoResponse om, String locationName) {
        CurrentWeatherResponse response = new CurrentWeatherResponse();
        
        // Location
        CurrentWeatherResponse.Location location = new CurrentWeatherResponse.Location();
        location.setName(locationName);
        location.setRegion("");
        location.setCountry("");
        location.setLat(om.getLatitude());
        location.setLon(om.getLongitude());
        location.setTzId(om.getTimezone());
        location.setLocalTime(om.getCurrent().getTime());
        response.setLocation(location);
        
        // Current weather
        CurrentWeatherResponse.Current current = new CurrentWeatherResponse.Current();
        current.setLastUpdated(om.getCurrent().getTime());
        current.setTempC(om.getCurrent().getTemperature_2m());
        current.setTempF(celsiusToFahrenheit(om.getCurrent().getTemperature_2m()));
        current.setIsDay(om.getCurrent().getIs_day());
        
        // Condition
        CurrentWeatherResponse.Condition condition = new CurrentWeatherResponse.Condition();
        condition.setText(OpenMeteoService.getWeatherDescription(om.getCurrent().getWeather_code()));
        condition.setIcon("//cdn.weatherapi.com/weather/64x64/day/" + 
                         OpenMeteoService.getWeatherIconCode(om.getCurrent().getWeather_code(), om.getCurrent().getIs_day()) + ".png");
        condition.setCode(om.getCurrent().getWeather_code());
        current.setCondition(condition);
        
        current.setWindKph(om.getCurrent().getWind_speed_10m());
        current.setWindMph(kmhToMph(om.getCurrent().getWind_speed_10m()));
        current.setWindDegree(om.getCurrent().getWind_direction_10m());
        current.setWindDir(OpenMeteoService.getWindDirection(om.getCurrent().getWind_direction_10m()));
        current.setPressureMb(om.getCurrent().getPressure_msl());
        current.setPressureIn(mbToInHg(om.getCurrent().getPressure_msl()));
        current.setPrecipMm(om.getCurrent().getPrecipitation());
        current.setPrecipIn(mmToInches(om.getCurrent().getPrecipitation()));
        current.setHumidity(om.getCurrent().getRelative_humidity_2m());
        current.setCloud(om.getCurrent().getCloud_cover());
        current.setFeelsLikeC(om.getCurrent().getApparent_temperature());
        current.setFeelsLikeF(celsiusToFahrenheit(om.getCurrent().getApparent_temperature()));
        current.setGustKph(om.getCurrent().getWind_gusts_10m());
        current.setGustMph(kmhToMph(om.getCurrent().getWind_gusts_10m()));
        
        response.setCurrent(current);
        
        return response;
    }
    
    private ForecastResponse transformToForecast(OpenMeteoResponse om, String locationName) {
        ForecastResponse response = new ForecastResponse();
        
        // Location and current (reuse transformation)
        CurrentWeatherResponse currentResponse = transformToCurrentWeather(om, locationName);
        response.setLocation(currentResponse.getLocation());
        response.setCurrent(currentResponse.getCurrent());
        
        // Forecast
        ForecastResponse.Forecast forecast = new ForecastResponse.Forecast();
        List<ForecastResponse.ForecastDay> forecastDays = new ArrayList<>();
        
        if (om.getDaily() != null && om.getDaily().getTime() != null) {
            for (int i = 0; i < om.getDaily().getTime().size(); i++) {
                ForecastResponse.ForecastDay day = new ForecastResponse.ForecastDay();
                day.setDate(om.getDaily().getTime().get(i));
                day.setDateEpoch(0L); // Not provided by Open-Meteo
                
                // Day data
                ForecastResponse.Day dayData = new ForecastResponse.Day();
                dayData.setMaxTempC(om.getDaily().getTemperature_2m_max().get(i));
                dayData.setMaxTempF(celsiusToFahrenheit(om.getDaily().getTemperature_2m_max().get(i)));
                dayData.setMinTempC(om.getDaily().getTemperature_2m_min().get(i));
                dayData.setMinTempF(celsiusToFahrenheit(om.getDaily().getTemperature_2m_min().get(i)));
                dayData.setAvgTempC((om.getDaily().getTemperature_2m_max().get(i) + om.getDaily().getTemperature_2m_min().get(i)) / 2);
                dayData.setAvgTempF(celsiusToFahrenheit(dayData.getAvgTempC()));
                dayData.setMaxWindKph(om.getDaily().getWind_speed_10m_max().get(i));
                dayData.setMaxWindMph(kmhToMph(om.getDaily().getWind_speed_10m_max().get(i)));
                dayData.setTotalPrecipMm(om.getDaily().getPrecipitation_sum().get(i));
                dayData.setTotalPrecipIn(mmToInches(om.getDaily().getPrecipitation_sum().get(i)));
                dayData.setDailyChanceOfRain(om.getDaily().getPrecipitation_probability_max().get(i));
                dayData.setUv(om.getDaily().getUv_index_max().get(i));
                
                CurrentWeatherResponse.Condition dayCondition = new CurrentWeatherResponse.Condition();
                dayCondition.setText(OpenMeteoService.getWeatherDescription(om.getDaily().getWeather_code().get(i)));
                dayCondition.setIcon("//cdn.weatherapi.com/weather/64x64/day/" + 
                                    OpenMeteoService.getWeatherIconCode(om.getDaily().getWeather_code().get(i), 1) + ".png");
                dayCondition.setCode(om.getDaily().getWeather_code().get(i));
                dayData.setCondition(dayCondition);
                
                day.setDay(dayData);
                
                // Astro data
                ForecastResponse.Astro astro = new ForecastResponse.Astro();
                astro.setSunrise(om.getDaily().getSunrise().get(i).substring(11)); // Extract time
                astro.setSunset(om.getDaily().getSunset().get(i).substring(11));
                day.setAstro(astro);
                
                // Hourly data for this day
                day.setHour(extractHourlyForDay(om, om.getDaily().getTime().get(i)));
                
                forecastDays.add(day);
            }
        }
        
        forecast.setForecastDay(forecastDays);
        response.setForecast(forecast);
        
        return response;
    }
    
    private List<ForecastResponse.Hour> extractHourlyForDay(OpenMeteoResponse om, String date) {
        List<ForecastResponse.Hour> hours = new ArrayList<>();
        
        if (om.getHourly() == null || om.getHourly().getTime() == null) {
            return hours;
        }
        
        for (int i = 0; i < om.getHourly().getTime().size(); i++) {
            String hourTime = om.getHourly().getTime().get(i);
            if (hourTime.startsWith(date)) {
                ForecastResponse.Hour hour = new ForecastResponse.Hour();
                hour.setTime(hourTime);
                hour.setTimeEpoch(0L);
                hour.setTempC(om.getHourly().getTemperature_2m().get(i));
                hour.setTempF(celsiusToFahrenheit(om.getHourly().getTemperature_2m().get(i)));
                hour.setIsDay(om.getHourly().getIs_day().get(i));
                
                CurrentWeatherResponse.Condition condition = new CurrentWeatherResponse.Condition();
                condition.setText(OpenMeteoService.getWeatherDescription(om.getHourly().getWeather_code().get(i)));
                condition.setIcon("//cdn.weatherapi.com/weather/64x64/day/" + 
                                 OpenMeteoService.getWeatherIconCode(om.getHourly().getWeather_code().get(i), 
                                                                     om.getHourly().getIs_day().get(i)) + ".png");
                condition.setCode(om.getHourly().getWeather_code().get(i));
                hour.setCondition(condition);
                
                hour.setWindKph(om.getHourly().getWind_speed_10m().get(i));
                hour.setWindMph(kmhToMph(om.getHourly().getWind_speed_10m().get(i)));
                hour.setWindDegree(om.getHourly().getWind_direction_10m().get(i));
                hour.setWindDir(OpenMeteoService.getWindDirection(om.getHourly().getWind_direction_10m().get(i)));
                hour.setPrecipMm(om.getHourly().getPrecipitation().get(i));
                hour.setPrecipIn(mmToInches(om.getHourly().getPrecipitation().get(i)));
                hour.setHumidity(om.getHourly().getRelative_humidity_2m().get(i));
                hour.setFeelsLikeC(om.getHourly().getApparent_temperature().get(i));
                hour.setFeelsLikeF(celsiusToFahrenheit(om.getHourly().getApparent_temperature().get(i)));
                hour.setChanceOfRain(om.getHourly().getPrecipitation_probability().get(i));
                hour.setVisKm(om.getHourly().getVisibility().get(i) / 1000.0);
                hour.setVisMiles(hour.getVisKm() * 0.621371);
                hour.setUv(om.getHourly().getUv_index().get(i));
                
                hours.add(hour);
            }
        }
        
        return hours;
    }
    
    // Conversion utilities
    private Double celsiusToFahrenheit(Double celsius) {
        return celsius != null ? (celsius * 9.0 / 5.0) + 32.0 : null;
    }
    
    private Double kmhToMph(Double kmh) {
        return kmh != null ? kmh * 0.621371 : null;
    }
    
    private Double mbToInHg(Double mb) {
        return mb != null ? mb * 0.02953 : null;
    }
    
    private Double mmToInches(Double mm) {
        return mm != null ? mm * 0.0393701 : null;
    }
    
    @Cacheable(value = "uvIndex", key = "#location")
    public UvIndexResponse getUvIndex(String location) {
        try {
            log.info("Fetching UV index for location: {}", location);
            
            // Parse location
            Double lat, lon;
            
            if (location.contains(",")) {
                String[] parts = location.split(",");
                lat = Double.parseDouble(parts[0].trim());
                lon = Double.parseDouble(parts[1].trim());
            } else {
                GeocodingResponse geocoding = openMeteoService.searchLocation(location);
                if (geocoding.getResults() == null || geocoding.getResults().isEmpty()) {
                    throw new WeatherApiException("Location not found: " + location);
                }
                GeocodingResponse.Result firstResult = geocoding.getResults().get(0);
                lat = firstResult.getLatitude();
                lon = firstResult.getLongitude();
            }
            
            // Fetch weather data with UV
            OpenMeteoResponse omResponse = openMeteoService.getWeatherData(lat, lon, 1);
            
            // Get current UV from hourly data (find current hour)
            Double currentUv = 0.0;
            if (omResponse.getHourly() != null && omResponse.getHourly().getUv_index() != null) {
                // Get the first non-null UV value (current or next hour)
                for (Double uv : omResponse.getHourly().getUv_index()) {
                    if (uv != null && uv > 0) {
                        currentUv = uv;
                        break;
                    }
                }
            }
            
            // Get max UV from daily data
            Double maxUv = 0.0;
            if (omResponse.getDaily() != null && 
                omResponse.getDaily().getUv_index_max() != null && 
                !omResponse.getDaily().getUv_index_max().isEmpty()) {
                maxUv = omResponse.getDaily().getUv_index_max().get(0);
            }
            
            return UvIndexResponse.fromUvValue(currentUv, maxUv);
        } catch (Exception e) {
            log.error("Error fetching UV index: {}", e.getMessage());
            throw new WeatherApiException("Failed to fetch UV index: " + e.getMessage());
        }
    }
    
    @Cacheable(value = "precipitationMinutely", key = "#location")
    public PrecipitationMinutelyResponse getPrecipitationMinutely(String location) {
        try {
            log.info("Fetching minutely precipitation for location: {}", location);
            
            // Parse location
            Double lat, lon;
            
            if (location.contains(",")) {
                String[] parts = location.split(",");
                lat = Double.parseDouble(parts[0].trim());
                lon = Double.parseDouble(parts[1].trim());
            } else {
                GeocodingResponse geocoding = openMeteoService.searchLocation(location);
                if (geocoding.getResults() == null || geocoding.getResults().isEmpty()) {
                    throw new WeatherApiException("Location not found: " + location);
                }
                GeocodingResponse.Result firstResult = geocoding.getResults().get(0);
                lat = firstResult.getLatitude();
                lon = firstResult.getLongitude();
            }
            
            return openMeteoService.getPrecipitationMinutely(lat, lon);
        } catch (Exception e) {
            log.error("Error fetching minutely precipitation: {}", e.getMessage());
            throw new WeatherApiException("Failed to fetch precipitation data: " + e.getMessage());
        }
    }
    
    @Cacheable(value = "pollen", key = "#location")
    public PollenSummaryResponse getPollenForecast(String location) {
        try {
            log.info("Fetching pollen forecast for location: {}", location);
            
            Double lat, lon;
            if (location.contains(",")) {
                String[] parts = location.split(",");
                lat = Double.parseDouble(parts[0].trim());
                lon = Double.parseDouble(parts[1].trim());
            } else {
                GeocodingResponse geocoding = openMeteoService.searchLocation(location);
                if (geocoding.getResults() == null || geocoding.getResults().isEmpty()) {
                    throw new WeatherApiException("Location not found: " + location);
                }
                lat = geocoding.getResults().get(0).getLatitude();
                lon = geocoding.getResults().get(0).getLongitude();
            }
            
            PollenForecastResponse pollenData = openMeteoService.getPollenForecast(lat, lon);
            return healthInsightsService.summarizePollenData(pollenData);
        } catch (Exception e) {
            log.error("Error fetching pollen forecast: {}", e.getMessage());
            throw new WeatherApiException("Failed to fetch pollen data: " + e.getMessage());
        }
    }
    
    @Cacheable(value = "healthScore", key = "#location")
    public HealthScoreResponse getHealthScore(String location) {
        try {
            log.info("Calculating health score for location: {}", location);
            
            // Fetch all required data
            CurrentWeatherResponse weather = getCurrentWeather(location);
            UvIndexResponse uv = getUvIndex(location);
            PollenSummaryResponse pollen = getPollenForecast(location);
            
            return healthInsightsService.calculateHealthScore(weather, uv, pollen);
        } catch (Exception e) {
            log.error("Error calculating health score: {}", e.getMessage());
            throw new WeatherApiException("Failed to calculate health score: " + e.getMessage());
        }
    }
    
    @Cacheable(value = "insights", key = "#location")
    public WeatherInsightsResponse getWeatherInsights(String location) {
        try {
            log.info("Generating weather insights for location: {}", location);
            
            // Fetch all required data
            CurrentWeatherResponse weather = getCurrentWeather(location);
            HealthScoreResponse healthScore = getHealthScore(location);
            UvIndexResponse uv = getUvIndex(location);
            PollenSummaryResponse pollen = getPollenForecast(location);
            
            return healthInsightsService.generateInsights(weather, healthScore, uv, pollen);
        } catch (Exception e) {
            log.error("Error generating insights: {}", e.getMessage());
            throw new WeatherApiException("Failed to generate insights: " + e.getMessage());
        }
    }
}
