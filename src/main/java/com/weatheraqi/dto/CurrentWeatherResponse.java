package com.weatheraqi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CurrentWeatherResponse {
    private Location location;
    private Current current;
    
    @Data
    public static class Location {
        private String name;
        private String region;
        private String country;
        private Double lat;
        private Double lon;
        @JsonProperty("tz_id")
        private String tzId;
        @JsonProperty("localtime")
        private String localTime;
    }
    
    @Data
    public static class Current {
        @JsonProperty("last_updated")
        private String lastUpdated;
        @JsonProperty("temp_c")
        private Double tempC;
        @JsonProperty("temp_f")
        private Double tempF;
        @JsonProperty("is_day")
        private Integer isDay;
        private Condition condition;
        @JsonProperty("wind_mph")
        private Double windMph;
        @JsonProperty("wind_kph")
        private Double windKph;
        @JsonProperty("wind_degree")
        private Integer windDegree;
        @JsonProperty("wind_dir")
        private String windDir;
        @JsonProperty("pressure_mb")
        private Double pressureMb;
        @JsonProperty("pressure_in")
        private Double pressureIn;
        @JsonProperty("precip_mm")
        private Double precipMm;
        @JsonProperty("precip_in")
        private Double precipIn;
        private Integer humidity;
        private Integer cloud;
        @JsonProperty("feelslike_c")
        private Double feelsLikeC;
        @JsonProperty("feelslike_f")
        private Double feelsLikeF;
        @JsonProperty("vis_km")
        private Double visKm;
        @JsonProperty("vis_miles")
        private Double visMiles;
        private Double uv;
        @JsonProperty("gust_mph")
        private Double gustMph;
        @JsonProperty("gust_kph")
        private Double gustKph;
        @JsonProperty("air_quality")
        private AirQuality airQuality;
    }
    
    @Data
    public static class Condition {
        private String text;
        private String icon;
        private Integer code;
    }
    
    @Data
    public static class AirQuality {
        private Double co;
        private Double no2;
        private Double o3;
        private Double so2;
        private Double pm2_5;
        private Double pm10;
        @JsonProperty("us-epa-index")
        private Integer usEpaIndex;
        @JsonProperty("gb-defra-index")
        private Integer gbDefraIndex;
    }
}
