package com.weatheraqi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AqiDetailsResponse {
    private Integer aqi;
    private String category;
    private String dominantPollutant;
    private String stationName;
    private Double latitude;
    private Double longitude;
    private String lastUpdated;
    private Pollutants pollutants;
    private HealthImplications healthImplications;
    
    @Data
    public static class Pollutants {
        @JsonProperty("pm2_5")
        private Double pm25;
        private Double pm10;
        private Double o3;
        private Double no2;
        private Double so2;
        private Double co;
    }
    
    @Data
    public static class HealthImplications {
        private String level;
        private String message;
        private String cautionaryStatement;
    }
}
