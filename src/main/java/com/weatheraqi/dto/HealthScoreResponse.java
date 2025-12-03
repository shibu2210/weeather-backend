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
        if (score >= 85) return "Excellent";
        if (score >= 70) return "Good";
        if (score >= 50) return "Fair";
        if (score >= 30) return "Poor";
        return "Hazardous";
    }
    
    public static String getScoreColor(Integer score) {
        if (score >= 85) return "#4CAF50";
        if (score >= 70) return "#8BC34A";
        if (score >= 50) return "#FFC107";
        if (score >= 30) return "#FF9800";
        return "#F44336";
    }
}
