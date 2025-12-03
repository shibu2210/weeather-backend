package com.weatheraqi.dto;

import lombok.Data;
import java.util.List;

@Data
public class WeatherInsightsResponse {
    private String briefing;
    private List<String> highlights;
    private List<ActivityRecommendation> activities;
    private List<Alert> alerts;
    
    @Data
    public static class ActivityRecommendation {
        private String activity;
        private String suitability; // Excellent, Good, Fair, Poor
        private String reason;
        private String icon;
    }
    
    @Data
    public static class Alert {
        private String type; // warning, info, success
        private String message;
        private String icon;
    }
}
