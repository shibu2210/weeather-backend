package com.weatheraqi.service;

import com.weatheraqi.config.AqicnApiConfig;
import com.weatheraqi.dto.AqicnResponse;
import com.weatheraqi.dto.AqicnSearchResponse;
import com.weatheraqi.dto.AqiDetailsResponse;
import com.weatheraqi.exception.AqicnApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class AqicnService {
    
    private final RestTemplate restTemplate;
    private final AqicnApiConfig aqicnApiConfig;
    
    @Cacheable(value = "aqiCity", key = "#city")
    public AqiDetailsResponse getAqiByCity(String city) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(aqicnApiConfig.getBaseUrl() + "/feed/" + city + "/")
                    .queryParam("token", aqicnApiConfig.getToken())
                    .toUriString();
            
            log.info("Fetching AQI data for city: {}", city);
            AqicnResponse response = restTemplate.getForObject(url, AqicnResponse.class);
            
            if (response == null || !"ok".equals(response.getStatus())) {
                throw new AqicnApiException("No AQI data received for city: " + city);
            }
            
            return convertToAqiDetails(response);
        } catch (Exception e) {
            log.error("Error fetching AQI for city {}: {}", city, e.getMessage());
            throw new AqicnApiException("Failed to fetch AQI data: " + e.getMessage());
        }
    }
    
    @Cacheable(value = "aqiCoordinates", key = "#lat + '_' + #lon")
    public AqiDetailsResponse getAqiByCoordinates(Double lat, Double lon) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(aqicnApiConfig.getBaseUrl() + "/feed/geo:" + lat + ";" + lon + "/")
                    .queryParam("token", aqicnApiConfig.getToken())
                    .toUriString();
            
            log.info("Fetching AQI data for coordinates: {}, {}", lat, lon);
            AqicnResponse response = restTemplate.getForObject(url, AqicnResponse.class);
            
            if (response == null || !"ok".equals(response.getStatus())) {
                throw new AqicnApiException("No AQI data received for coordinates");
            }
            
            return convertToAqiDetails(response);
        } catch (Exception e) {
            log.error("Error fetching AQI for coordinates {},{}: {}", lat, lon, e.getMessage());
            throw new AqicnApiException("Failed to fetch AQI data: " + e.getMessage());
        }
    }
    
    @Cacheable(value = "aqiSearch", key = "#keyword")
    public AqicnSearchResponse searchAqiStations(String keyword) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(aqicnApiConfig.getBaseUrl() + "/search/")
                    .queryParam("token", aqicnApiConfig.getToken())
                    .queryParam("keyword", keyword)
                    .toUriString();
            
            log.info("Searching AQI stations for keyword: {}", keyword);
            AqicnSearchResponse response = restTemplate.getForObject(url, AqicnSearchResponse.class);
            
            if (response == null || !"ok".equals(response.getStatus())) {
                throw new AqicnApiException("No search results for keyword: " + keyword);
            }
            
            return response;
        } catch (Exception e) {
            log.error("Error searching AQI stations for {}: {}", keyword, e.getMessage());
            throw new AqicnApiException("Failed to search AQI stations: " + e.getMessage());
        }
    }
    
    private AqiDetailsResponse convertToAqiDetails(AqicnResponse response) {
        AqiDetailsResponse details = new AqiDetailsResponse();
        AqicnResponse.AqiData data = response.getData();
        
        // Basic AQI info
        details.setAqi(data.getAqi());
        details.setCategory(getAqiCategory(data.getAqi()));
        details.setDominantPollutant(data.getDominentPol());
        
        // Station info
        if (data.getCity() != null) {
            details.setStationName(data.getCity().getName());
            if (data.getCity().getGeo() != null && data.getCity().getGeo().length == 2) {
                details.setLatitude(data.getCity().getGeo()[0]);
                details.setLongitude(data.getCity().getGeo()[1]);
            }
        }
        
        // Time
        if (data.getTime() != null) {
            details.setLastUpdated(data.getTime().getS());
        }
        
        // Pollutants
        AqiDetailsResponse.Pollutants pollutants = new AqiDetailsResponse.Pollutants();
        if (data.getIaqi() != null) {
            pollutants.setPm25(getPollutantValue(data.getIaqi(), "pm25"));
            pollutants.setPm10(getPollutantValue(data.getIaqi(), "pm10"));
            pollutants.setO3(getPollutantValue(data.getIaqi(), "o3"));
            pollutants.setNo2(getPollutantValue(data.getIaqi(), "no2"));
            pollutants.setSo2(getPollutantValue(data.getIaqi(), "so2"));
            pollutants.setCo(getPollutantValue(data.getIaqi(), "co"));
        }
        details.setPollutants(pollutants);
        
        // Health implications
        details.setHealthImplications(getHealthImplications(data.getAqi()));
        
        return details;
    }
    
    private Double getPollutantValue(java.util.Map<String, AqicnResponse.AqiData.PollutantValue> iaqi, String pollutant) {
        if (iaqi.containsKey(pollutant)) {
            AqicnResponse.AqiData.PollutantValue value = iaqi.get(pollutant);
            return value != null ? value.getV() : null;
        }
        return null;
    }
    
    private String getAqiCategory(Integer aqi) {
        if (aqi == null) return "Unknown";
        if (aqi <= 50) return "Good";
        if (aqi <= 100) return "Moderate";
        if (aqi <= 150) return "Unhealthy for Sensitive Groups";
        if (aqi <= 200) return "Unhealthy";
        if (aqi <= 300) return "Very Unhealthy";
        return "Hazardous";
    }
    
    private AqiDetailsResponse.HealthImplications getHealthImplications(Integer aqi) {
        AqiDetailsResponse.HealthImplications health = new AqiDetailsResponse.HealthImplications();
        
        if (aqi == null) {
            health.setLevel("Unknown");
            health.setMessage("AQI data not available");
            health.setCautionaryStatement("Check back later for updates");
            return health;
        }
        
        if (aqi <= 50) {
            health.setLevel("Good");
            health.setMessage("Air quality is satisfactory, and air pollution poses little or no risk.");
            health.setCautionaryStatement("None");
        } else if (aqi <= 100) {
            health.setLevel("Moderate");
            health.setMessage("Air quality is acceptable. However, there may be a risk for some people, particularly those who are unusually sensitive to air pollution.");
            health.setCautionaryStatement("Unusually sensitive people should consider limiting prolonged outdoor exertion.");
        } else if (aqi <= 150) {
            health.setLevel("Unhealthy for Sensitive Groups");
            health.setMessage("Members of sensitive groups may experience health effects. The general public is less likely to be affected.");
            health.setCautionaryStatement("Active children and adults, and people with respiratory disease, such as asthma, should limit prolonged outdoor exertion.");
        } else if (aqi <= 200) {
            health.setLevel("Unhealthy");
            health.setMessage("Some members of the general public may experience health effects; members of sensitive groups may experience more serious health effects.");
            health.setCautionaryStatement("Active children and adults, and people with respiratory disease, such as asthma, should avoid prolonged outdoor exertion; everyone else, especially children, should limit prolonged outdoor exertion.");
        } else if (aqi <= 300) {
            health.setLevel("Very Unhealthy");
            health.setMessage("Health alert: The risk of health effects is increased for everyone.");
            health.setCautionaryStatement("Active children and adults, and people with respiratory disease, such as asthma, should avoid all outdoor exertion; everyone else, especially children, should limit outdoor exertion.");
        } else {
            health.setLevel("Hazardous");
            health.setMessage("Health warning of emergency conditions: everyone is more likely to be affected.");
            health.setCautionaryStatement("Everyone should avoid all outdoor exertion.");
        }
        
        return health;
    }
}
