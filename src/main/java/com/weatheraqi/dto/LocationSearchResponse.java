package com.weatheraqi.dto;

import lombok.Data;

@Data
public class LocationSearchResponse {
    private Long id;
    private String name;
    private String region;
    private String country;
    private Double lat;
    private Double lon;
    private String url;
}
