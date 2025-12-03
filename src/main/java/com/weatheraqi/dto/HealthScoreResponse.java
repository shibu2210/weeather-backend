package com.weatheraqi.dto;

import lombok.Data;
import java.util.List;

@Data
public class HealthScoreResponse {
    private Integer score; // 0-100
    private String level; // Excellent, Good, Fair, Poor, Hazardous
    private String color;
    private String summary;
    private List<Factor> factors;
    private List<String> recommendations;
    private String bestTimeForOutdoor;
    
    @Data
    public static class Factor {
        private String name;
        private Integer score;
        private String status; // Good, Moderate, Poor
        private String icon;
    }
    
    public static String getScoreLevel(Integer score) {
        if (score >= 80) return "Excellent";
        if (score >= 60) return "Good";
        if (score >= 40) return "Fair";
        if (score >= 20) return "Poor";
        return "Hazardous";
    }
    
    public static String getScoreColor(Integer score) {
        if (score >= 80) return "#4CAF50";
        if (score >= 60) return "#8BC34A";
        if (score >= 40) return "#FFC107";
        if (score >= 20) return "#FF9800";
        return "#F44336";
    }
}
