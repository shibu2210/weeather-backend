package com.weatheraqi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ForecastResponse {
    private CurrentWeatherResponse.Location location;
    private CurrentWeatherResponse.Current current;
    private Forecast forecast;
    
    @Data
    public static class Forecast {
        @JsonProperty("forecastday")
        private List<ForecastDay> forecastDay;
    }
    
    @Data
    public static class ForecastDay {
        private String date;
        @JsonProperty("date_epoch")
        private Long dateEpoch;
        private Day day;
        private Astro astro;
        private List<Hour> hour;
    }
    
    @Data
    public static class Day {
        @JsonProperty("maxtemp_c")
        private Double maxTempC;
        @JsonProperty("maxtemp_f")
        private Double maxTempF;
        @JsonProperty("mintemp_c")
        private Double minTempC;
        @JsonProperty("mintemp_f")
        private Double minTempF;
        @JsonProperty("avgtemp_c")
        private Double avgTempC;
        @JsonProperty("avgtemp_f")
        private Double avgTempF;
        @JsonProperty("maxwind_mph")
        private Double maxWindMph;
        @JsonProperty("maxwind_kph")
        private Double maxWindKph;
        @JsonProperty("totalprecip_mm")
        private Double totalPrecipMm;
        @JsonProperty("totalprecip_in")
        private Double totalPrecipIn;
        @JsonProperty("avgvis_km")
        private Double avgVisKm;
        @JsonProperty("avgvis_miles")
        private Double avgVisMiles;
        @JsonProperty("avghumidity")
        private Double avgHumidity;
        @JsonProperty("daily_will_it_rain")
        private Integer dailyWillItRain;
        @JsonProperty("daily_chance_of_rain")
        private Integer dailyChanceOfRain;
        @JsonProperty("daily_will_it_snow")
        private Integer dailyWillItSnow;
        @JsonProperty("daily_chance_of_snow")
        private Integer dailyChanceOfSnow;
        private CurrentWeatherResponse.Condition condition;
        private Double uv;
        @JsonProperty("air_quality")
        private CurrentWeatherResponse.AirQuality airQuality;
    }
    
    @Data
    public static class Astro {
        private String sunrise;
        private String sunset;
        private String moonrise;
        private String moonset;
        @JsonProperty("moon_phase")
        private String moonPhase;
        @JsonProperty("moon_illumination")
        private Integer moonIllumination;
    }
    
    @Data
    public static class Hour {
        @JsonProperty("time_epoch")
        private Long timeEpoch;
        private String time;
        @JsonProperty("temp_c")
        private Double tempC;
        @JsonProperty("temp_f")
        private Double tempF;
        @JsonProperty("is_day")
        private Integer isDay;
        private CurrentWeatherResponse.Condition condition;
        @JsonProperty("wind_mph")
        private Double windMph;
        @JsonProperty("wind_kph")
        private Double windKph;
        @JsonProperty("wind_degree")
        private Integer windDegree;
        @JsonProperty("wind_dir")
        private String windDir;
        @JsonProperty("pressure_mb")
        private Double pressureMb;
        @JsonProperty("pressure_in")
        private Double pressureIn;
        @JsonProperty("precip_mm")
        private Double precipMm;
        @JsonProperty("precip_in")
        private Double precipIn;
        private Integer humidity;
        private Integer cloud;
        @JsonProperty("feelslike_c")
        private Double feelsLikeC;
        @JsonProperty("feelslike_f")
        private Double feelsLikeF;
        @JsonProperty("windchill_c")
        private Double windChillC;
        @JsonProperty("windchill_f")
        private Double windChillF;
        @JsonProperty("heatindex_c")
        private Double heatIndexC;
        @JsonProperty("heatindex_f")
        private Double heatIndexF;
        @JsonProperty("dewpoint_c")
        private Double dewPointC;
        @JsonProperty("dewpoint_f")
        private Double dewPointF;
        @JsonProperty("will_it_rain")
        private Integer willItRain;
        @JsonProperty("chance_of_rain")
        private Integer chanceOfRain;
        @JsonProperty("will_it_snow")
        private Integer willItSnow;
        @JsonProperty("chance_of_snow")
        private Integer chanceOfSnow;
        @JsonProperty("vis_km")
        private Double visKm;
        @JsonProperty("vis_miles")
        private Double visMiles;
        @JsonProperty("gust_mph")
        private Double gustMph;
        @JsonProperty("gust_kph")
        private Double gustKph;
        private Double uv;
        @JsonProperty("air_quality")
        private CurrentWeatherResponse.AirQuality airQuality;
    }
}
