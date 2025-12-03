package com.weatheraqi.dto;

import lombok.Data;

@Data
public class UvIndexResponse {
    private Double currentUv;
    private Double maxUvToday;
    private String uvLevel;
    private String recommendation;
    private String color;
    
    public static UvIndexResponse fromUvValue(Double uv, Double maxUv) {
        UvIndexResponse response = new UvIndexResponse();
        response.setCurrentUv(uv);
        response.setMaxUvToday(maxUv);
        
        if (uv == null || uv < 0) {
            response.setUvLevel("Unknown");
            response.setRecommendation("UV data unavailable");
            response.setColor("#gray");
            return response;
        }
        
        if (uv < 3) {
            response.setUvLevel("Low");
            response.setRecommendation("No protection needed. You can safely stay outside.");
            response.setColor("#4CAF50");
        } else if (uv < 6) {
            response.setUvLevel("Moderate");
            response.setRecommendation("Seek shade during midday hours. Wear sunscreen.");
            response.setColor("#FFC107");
        } else if (uv < 8) {
            response.setUvLevel("High");
            response.setRecommendation("Protection essential. Wear sunscreen, hat, and sunglasses.");
            response.setColor("#FF9800");
        } else if (uv < 11) {
            response.setUvLevel("Very High");
            response.setRecommendation("Extra protection needed. Avoid sun during midday.");
            response.setColor("#F44336");
        } else {
            response.setUvLevel("Extreme");
            response.setRecommendation("Take all precautions. Avoid sun exposure if possible.");
            response.setColor("#9C27B0");
        }
        
        return response;
    }
}
