package com.weatheraqi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
public class AqicnResponse {
    private String status;
    private AqiData data;
    
    @Data
    public static class AqiData {
        private Integer aqi;
        private Integer idx;
        private City city;
        
        @JsonProperty("dominentpol")
        private String dominentPol;
        
        private Map<String, PollutantValue> iaqi;
        private Time time;
        
        @Data
        public static class City {
            private String name;
            private double[] geo;
            private String url;
        }
        
        @Data
        public static class PollutantValue {
            private Double v;
        }
        
        @Data
        public static class Time {
            private String s;
            private String tz;
            
            @JsonProperty("v")
            private Long timestamp;
        }
    }
}
