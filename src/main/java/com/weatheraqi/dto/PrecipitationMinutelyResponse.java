package com.weatheraqi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class PrecipitationMinutelyResponse {
    private Double latitude;
    private Double longitude;
    @JsonProperty("generationtime_ms")
    private Double generationTimeMs;
    @JsonProperty("utc_offset_seconds")
    private Integer utcOffsetSeconds;
    private String timezone;
    @JsonProperty("timezone_abbreviation")
    private String timezoneAbbreviation;
    private Minutely15 minutely_15;
    
    @Data
    public static class Minutely15 {
        private List<String> time;
        private List<Double> precipitation;
    }
}
