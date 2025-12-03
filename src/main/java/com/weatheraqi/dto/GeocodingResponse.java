package com.weatheraqi.dto;

import lombok.Data;
import java.util.List;

@Data
public class GeocodingResponse {
    private List<Result> results;
    
    @Data
    public static class Result {
        private Long id;
        private String name;
        private Double latitude;
        private Double longitude;
        private Double elevation;
        private String feature_code;
        private String country_code;
        private String country;
        private String timezone;
        private Long population;
        private String admin1;
        private String admin2;
        private String admin3;
        private String admin4;
    }
}
