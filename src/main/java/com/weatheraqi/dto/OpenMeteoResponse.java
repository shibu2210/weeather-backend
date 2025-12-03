package com.weatheraqi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class OpenMeteoResponse {
    private Double latitude;
    private Double longitude;
    @JsonProperty("generationtime_ms")
    private Double generationTimeMs;
    @JsonProperty("utc_offset_seconds")
    private Integer utcOffsetSeconds;
    private String timezone;
    @JsonProperty("timezone_abbreviation")
    private String timezoneAbbreviation;
    private Double elevation;
    @JsonProperty("current_units")
    private CurrentUnits currentUnits;
    private Current current;
    @JsonProperty("hourly_units")
    private HourlyUnits hourlyUnits;
    private Hourly hourly;
    @JsonProperty("daily_units")
    private DailyUnits dailyUnits;
    private Daily daily;
    
    @Data
    public static class CurrentUnits {
        private String time;
        private String interval;
        private String temperature_2m;
        private String relative_humidity_2m;
        private String apparent_temperature;
        private String is_day;
        private String precipitation;
        private String weather_code;
        private String cloud_cover;
        private String pressure_msl;
        private String surface_pressure;
        private String wind_speed_10m;
        private String wind_direction_10m;
        private String wind_gusts_10m;
    }
    
    @Data
    public static class Current {
        private String time;
        private Integer interval;
        private Double temperature_2m;
        private Integer relative_humidity_2m;
        private Double apparent_temperature;
        private Integer is_day;
        private Double precipitation;
        private Integer weather_code;
        private Integer cloud_cover;
        private Double pressure_msl;
        private Double surface_pressure;
        private Double wind_speed_10m;
        private Integer wind_direction_10m;
        private Double wind_gusts_10m;
    }
    
    @Data
    public static class HourlyUnits {
        private String time;
        private String temperature_2m;
        private String relative_humidity_2m;
        private String apparent_temperature;
        private String precipitation_probability;
        private String precipitation;
        private String weather_code;
        private String visibility;
        private String wind_speed_10m;
        private String wind_direction_10m;
        private String uv_index;
        private String is_day;
    }
    
    @Data
    public static class Hourly {
        private List<String> time;
        private List<Double> temperature_2m;
        private List<Integer> relative_humidity_2m;
        private List<Double> apparent_temperature;
        private List<Integer> precipitation_probability;
        private List<Double> precipitation;
        private List<Integer> weather_code;
        private List<Double> visibility;
        private List<Double> wind_speed_10m;
        private List<Integer> wind_direction_10m;
        private List<Double> uv_index;
        private List<Integer> is_day;
    }
    
    @Data
    public static class DailyUnits {
        private String time;
        private String weather_code;
        private String temperature_2m_max;
        private String temperature_2m_min;
        private String apparent_temperature_max;
        private String apparent_temperature_min;
        private String sunrise;
        private String sunset;
        private String uv_index_max;
        private String precipitation_sum;
        private String precipitation_probability_max;
        private String wind_speed_10m_max;
        private String wind_gusts_10m_max;
        private String wind_direction_10m_dominant;
    }
    
    @Data
    public static class Daily {
        private List<String> time;
        private List<Integer> weather_code;
        private List<Double> temperature_2m_max;
        private List<Double> temperature_2m_min;
        private List<Double> apparent_temperature_max;
        private List<Double> apparent_temperature_min;
        private List<String> sunrise;
        private List<String> sunset;
        private List<Double> uv_index_max;
        private List<Double> precipitation_sum;
        private List<Integer> precipitation_probability_max;
        private List<Double> wind_speed_10m_max;
        private List<Double> wind_gusts_10m_max;
        private List<Integer> wind_direction_10m_dominant;
    }
}
