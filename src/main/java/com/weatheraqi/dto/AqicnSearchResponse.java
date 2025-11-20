package com.weatheraqi.dto;

import lombok.Data;
import java.util.List;

@Data
public class AqicnSearchResponse {
    private String status;
    private List<StationData> data;
    
    @Data
    public static class StationData {
        private Integer uid;
        private String aqi;
        private Time time;
        private Station station;
        
        @Data
        public static class Time {
            private String stime;
            private String tz;
        }
        
        @Data
        public static class Station {
            private String name;
            private double[] geo;
            private String url;
        }
    }
}
