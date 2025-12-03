package com.weatheraqi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class PollenForecastResponse {
    private Double latitude;
    private Double longitude;
    @JsonProperty("generationtime_ms")
    private Double generationTimeMs;
    @JsonProperty("utc_offset_seconds")
    private Integer utcOffsetSeconds;
    private String timezone;
    @JsonProperty("daily_units")
    private DailyUnits dailyUnits;
    private Daily daily;
    
    @Data
    public static class DailyUnits {
        private String time;
        private String alder_pollen;
        private String birch_pollen;
        private String grass_pollen;
        private String mugwort_pollen;
        private String olive_pollen;
        private String ragweed_pollen;
    }
    
    @Data
    public static class Daily {
        private List<String> time;
        private List<Double> alder_pollen;
        private List<Double> birch_pollen;
        private List<Double> grass_pollen;
        private List<Double> mugwort_pollen;
        private List<Double> olive_pollen;
        private List<Double> ragweed_pollen;
    }
}
