package com.weatheraqi.dto;

import lombok.Data;
import java.util.Map;

@Data
public class PollenSummaryResponse {
    private String overallLevel;
    private String overallRisk;
    private String recommendation;
    private Map<String, PollenDetail> pollens;
    
    @Data
    public static class PollenDetail {
        private String name;
        private Double level;
        private String risk;
        private String color;
    }
    
    public static String getRiskLevel(Double level) {
        if (level == null || level < 20) return "Low";
        if (level < 50) return "Moderate";
        if (level < 100) return "High";
        return "Very High";
    }
    
    public static String getRiskColor(String risk) {
        switch (risk) {
            case "Low": return "#4CAF50";
            case "Moderate": return "#FFC107";
            case "High": return "#FF9800";
            case "Very High": return "#F44336";
            default: return "#9E9E9E";
        }
    }
}
