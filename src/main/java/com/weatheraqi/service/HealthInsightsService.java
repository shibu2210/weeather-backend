package com.weatheraqi.service;

import com.weatheraqi.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthInsightsService {
    
    public PollenSummaryResponse summarizePollenData(PollenForecastResponse pollenData) {
        PollenSummaryResponse summary = new PollenSummaryResponse();
        Map<String, PollenSummaryResponse.PollenDetail> pollens = new LinkedHashMap<>();
        
        if (pollenData.getDaily() == null) {
            summary.setOverallLevel("Unknown");
            summary.setOverallRisk("Data unavailable");
            summary.setPollens(pollens);
            return summary;
        }
        
        // Get today's pollen levels
        Double alder = getFirstValue(pollenData.getDaily().getAlder_pollen());
        Double birch = getFirstValue(pollenData.getDaily().getBirch_pollen());
        Double grass = getFirstValue(pollenData.getDaily().getGrass_pollen());
        Double mugwort = getFirstValue(pollenData.getDaily().getMugwort_pollen());
        Double olive = getFirstValue(pollenData.getDaily().getOlive_pollen());
        Double ragweed = getFirstValue(pollenData.getDaily().getRagweed_pollen());
        
        // Add pollen details
        addPollenDetail(pollens, "Grass", grass);
        addPollenDetail(pollens, "Birch", birch);
        addPollenDetail(pollens, "Ragweed", ragweed);
        addPollenDetail(pollens, "Olive", olive);
        addPollenDetail(pollens, "Alder", alder);
        addPollenDetail(pollens, "Mugwort", mugwort);
        
        // Calculate overall level
        Double maxPollen = Collections.max(Arrays.asList(alder, birch, grass, mugwort, olive, ragweed));
        String overallRisk = PollenSummaryResponse.getRiskLevel(maxPollen);
        
        summary.setOverallLevel(String.format("%.0f", maxPollen));
        summary.setOverallRisk(overallRisk);
        summary.setPollens(pollens);
        summary.setRecommendation(getPollenRecommendation(overallRisk));
        
        return summary;
    }
    
    private void addPollenDetail(Map<String, PollenSummaryResponse.PollenDetail> pollens, String name, Double level) {
        PollenSummaryResponse.PollenDetail detail = new PollenSummaryResponse.PollenDetail();
        detail.setName(name);
        detail.setLevel(level);
        String risk = PollenSummaryResponse.getRiskLevel(level);
        detail.setRisk(risk);
        detail.setColor(PollenSummaryResponse.getRiskColor(risk));
        pollens.put(name.toLowerCase(), detail);
    }
    
    private Double getFirstValue(List<Double> values) {
        return (values != null && !values.isEmpty() && values.get(0) != null) ? values.get(0) : 0.0;
    }
    
    private String getPollenRecommendation(String risk) {
        switch (risk) {
            case "Low":
                return "Pollen levels are low. Great day for outdoor activities!";
            case "Moderate":
                return "Moderate pollen levels. Consider taking allergy medication if sensitive.";
            case "High":
                return "High pollen levels. Allergy sufferers should limit outdoor exposure.";
            case "Very High":
                return "Very high pollen levels. Stay indoors if possible and keep windows closed.";
            default:
                return "Pollen data unavailable.";
        }
    }
    
    public HealthScoreResponse calculateHealthScore(
            CurrentWeatherResponse weather,
            UvIndexResponse uv,
            PollenSummaryResponse pollen) {
        
        HealthScoreResponse response = new HealthScoreResponse();
        List<HealthScoreResponse.Factor> factors = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        // Calculate individual scores
        int aqiScore = calculateAqiScore(weather);
        int uvScore = calculateUvScore(uv);
        int pollenScore = calculatePollenScore(pollen);
        int weatherScore = calculateWeatherScore(weather);
        
        // Add factors
        factors.add(createFactor("Air Quality", aqiScore, "🌫️"));
        factors.add(createFactor("UV Index", uvScore, "☀️"));
        factors.add(createFactor("Pollen", pollenScore, "🌸"));
        factors.add(createFactor("Weather", weatherScore, "🌤️"));
        
        // Calculate overall score with dynamic weighting
        // When AQI is poor (unhealthy), it should dominate the score
        int overallScore;
        if (aqiScore < 50) {
            // Poor AQI: Give it 60% weight, others share 40%
            overallScore = (aqiScore * 60 + uvScore * 15 + pollenScore * 13 + weatherScore * 12) / 100;
        } else if (aqiScore < 70) {
            // Moderate AQI: Give it 45% weight
            overallScore = (aqiScore * 45 + uvScore * 20 + pollenScore * 18 + weatherScore * 17) / 100;
        } else {
            // Good AQI: Standard weighting
            overallScore = (aqiScore * 35 + uvScore * 25 + pollenScore * 20 + weatherScore * 20) / 100;
        }
        
        response.setScore(overallScore);
        response.setLevel(HealthScoreResponse.getScoreLevel(overallScore));
        response.setColor(HealthScoreResponse.getScoreColor(overallScore));
        response.setFactors(factors);
        
        // Generate summary
        response.setSummary(generateHealthSummary(overallScore, weather));
        
        // Generate recommendations
        if (aqiScore < 60) recommendations.add("Air quality is poor. Consider wearing a mask outdoors.");
        if (uvScore < 60) recommendations.add("UV levels are high. Apply sunscreen and wear protective clothing.");
        if (pollenScore < 60) recommendations.add("Pollen levels are elevated. Take allergy medication if needed.");
        if (recommendations.isEmpty()) recommendations.add("Conditions are excellent for outdoor activities!");
        
        response.setRecommendations(recommendations);
        response.setBestTimeForOutdoor(determineBestTime(weather));
        
        return response;
    }
    
    private int calculateAqiScore(CurrentWeatherResponse weather) {
        if (weather.getCurrent() == null || weather.getCurrent().getAirQuality() == null) {
            return 100;
        }
        Integer aqi = weather.getCurrent().getAirQuality().getAqi();
        if (aqi == null) return 100;
        
        // Convert AQI to score (inverse relationship) - more strict for health
        // AQI 0-50 (Good) = 100-90
        // AQI 51-100 (Moderate) = 89-70
        // AQI 101-150 (Unhealthy for Sensitive) = 69-40
        // AQI 151-200 (Unhealthy) = 39-20
        // AQI 201-300 (Very Unhealthy) = 19-5
        // AQI 301+ (Hazardous) = 0-4
        
        if (aqi <= 50) {
            // Good: 100-90
            return 100 - (int)((aqi / 50.0) * 10);
        } else if (aqi <= 100) {
            // Moderate: 89-70
            return 89 - (int)(((aqi - 51) / 49.0) * 19);
        } else if (aqi <= 150) {
            // Unhealthy for Sensitive: 69-40
            return 69 - (int)(((aqi - 101) / 49.0) * 29);
        } else if (aqi <= 200) {
            // Unhealthy: 39-20
            return 39 - (int)(((aqi - 151) / 49.0) * 19);
        } else if (aqi <= 300) {
            // Very Unhealthy: 19-5
            return 19 - (int)(((aqi - 201) / 99.0) * 14);
        } else {
            // Hazardous: 0-4
            return Math.max(0, 4 - (int)((aqi - 301) / 100.0));
        }
    }
    
    private int calculateUvScore(UvIndexResponse uv) {
        if (uv == null || uv.getCurrentUv() == null) return 100;
        
        Double uvIndex = uv.getCurrentUv();
        if (uvIndex < 3) return 100;
        if (uvIndex < 6) return 80;
        if (uvIndex < 8) return 60;
        if (uvIndex < 11) return 40;
        return 20;
    }
    
    private int calculatePollenScore(PollenSummaryResponse pollen) {
        if (pollen == null || pollen.getOverallRisk() == null) return 100;
        
        switch (pollen.getOverallRisk()) {
            case "Low": return 100;
            case "Moderate": return 75;
            case "High": return 50;
            case "Very High": return 25;
            default: return 100;
        }
    }
    
    private int calculateWeatherScore(CurrentWeatherResponse weather) {
        if (weather.getCurrent() == null) return 100;
        
        int score = 100;
        
        // Temperature comfort (20-25°C is ideal)
        Double temp = weather.getCurrent().getTempC();
        if (temp != null) {
            if (temp < 0 || temp > 35) score -= 30;
            else if (temp < 10 || temp > 30) score -= 15;
        }
        
        // Precipitation
        Double precip = weather.getCurrent().getPrecipMm();
        if (precip != null && precip > 0) {
            score -= 20;
        }
        
        // Wind
        Double wind = weather.getCurrent().getWindKph();
        if (wind != null && wind > 30) {
            score -= 15;
        }
        
        return Math.max(score, 0);
    }
    
    private HealthScoreResponse.Factor createFactor(String name, int score, String icon) {
        HealthScoreResponse.Factor factor = new HealthScoreResponse.Factor();
        factor.setName(name);
        factor.setScore(score);
        factor.setIcon(icon);
        
        if (score >= 80) factor.setStatus("Good");
        else if (score >= 60) factor.setStatus("Moderate");
        else factor.setStatus("Poor");
        
        return factor;
    }
    
    private String generateHealthSummary(int score, CurrentWeatherResponse weather) {
        String level = HealthScoreResponse.getScoreLevel(score);
        String condition = weather.getCurrent().getCondition().getText().toLowerCase();
        
        if (score >= 80) {
            return String.format("Excellent conditions for outdoor activities! %s and healthy air quality.", 
                capitalize(condition));
        } else if (score >= 60) {
            return "Good conditions overall. Some factors may affect sensitive individuals.";
        } else if (score >= 40) {
            return "Fair conditions. Consider limiting prolonged outdoor exposure.";
        } else if (score >= 20) {
            return "Poor conditions. Outdoor activities not recommended for sensitive groups.";
        } else {
            return "Hazardous conditions. Avoid outdoor activities if possible.";
        }
    }
    
    private String determineBestTime(CurrentWeatherResponse weather) {
        // Simple logic - can be enhanced with hourly data
        return "10:00 AM - 2:00 PM";
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    public WeatherInsightsResponse generateInsights(
            CurrentWeatherResponse weather,
            HealthScoreResponse healthScore,
            UvIndexResponse uv,
            PollenSummaryResponse pollen) {
        
        WeatherInsightsResponse insights = new WeatherInsightsResponse();
        List<String> highlights = new ArrayList<>();
        List<WeatherInsightsResponse.ActivityRecommendation> activities = new ArrayList<>();
        List<WeatherInsightsResponse.Alert> alerts = new ArrayList<>();
        
        // Generate briefing
        String briefing = generateBriefing(weather, healthScore);
        insights.setBriefing(briefing);
        
        // Generate highlights
        highlights.add(String.format("Health Score: %d/100 (%s)", healthScore.getScore(), healthScore.getLevel()));
        highlights.add(String.format("Temperature: %.0f°C, feels like %.0f°C", 
            weather.getCurrent().getTempC(), weather.getCurrent().getFeelsLikeC()));
        
        if (uv.getCurrentUv() > 6) {
            highlights.add(String.format("High UV: %.1f - Sun protection needed", uv.getCurrentUv()));
        }
        
        insights.setHighlights(highlights);
        
        // Activity recommendations
        activities.add(createActivity("Running", healthScore.getScore(), weather, "🏃"));
        activities.add(createActivity("Cycling", healthScore.getScore(), weather, "🚴"));
        activities.add(createActivity("Outdoor Dining", healthScore.getScore(), weather, "🍽️"));
        
        insights.setActivities(activities);
        
        // Alerts
        // Check AQI specifically
        Integer aqi = weather.getCurrent().getAirQuality() != null ? 
                     weather.getCurrent().getAirQuality().getAqi() : null;
        if (aqi != null) {
            if (aqi > 200) {
                alerts.add(createAlert("warning", "Very unhealthy air quality - avoid outdoor activities", "🚨"));
            } else if (aqi > 150) {
                alerts.add(createAlert("warning", "Unhealthy air quality - limit outdoor exposure", "⚠️"));
            } else if (aqi > 100) {
                alerts.add(createAlert("info", "Moderate air quality - sensitive groups should limit prolonged outdoor exertion", "💨"));
            }
        }
        
        if (healthScore.getScore() < 40) {
            alerts.add(createAlert("warning", "Poor outdoor conditions detected", "⚠️"));
        }
        if (uv.getCurrentUv() > 8) {
            alerts.add(createAlert("warning", "Very high UV levels - protection essential", "☀️"));
        }
        if (weather.getCurrent().getPrecipMm() > 0) {
            alerts.add(createAlert("info", "Rain detected - bring an umbrella", "🌧️"));
        }
        
        insights.setAlerts(alerts);
        
        return insights;
    }
    
    private String generateBriefing(CurrentWeatherResponse weather, HealthScoreResponse healthScore) {
        String condition = weather.getCurrent().getCondition().getText();
        int score = healthScore.getScore();
        
        if (score >= 80) {
            return String.format("Perfect conditions today! %s with excellent air quality. Great time for outdoor activities.", 
                condition);
        } else if (score >= 60) {
            return String.format("%s today. Conditions are generally good with some minor concerns.", 
                condition);
        } else {
            return String.format("%s today. Consider limiting outdoor exposure due to health factors.", 
                condition);
        }
    }
    
    private WeatherInsightsResponse.ActivityRecommendation createActivity(
            String activity, int healthScore, CurrentWeatherResponse weather, String icon) {
        
        WeatherInsightsResponse.ActivityRecommendation rec = new WeatherInsightsResponse.ActivityRecommendation();
        rec.setActivity(activity);
        rec.setIcon(icon);
        
        if (healthScore >= 85) {
            rec.setSuitability("Excellent");
            rec.setReason("Perfect conditions");
        } else if (healthScore >= 70) {
            rec.setSuitability("Good");
            rec.setReason("Generally favorable");
        } else if (healthScore >= 50) {
            rec.setSuitability("Fair");
            rec.setReason("Some concerns present");
        } else {
            rec.setSuitability("Poor");
            rec.setReason("Not recommended");
        }
        
        return rec;
    }
    
    private WeatherInsightsResponse.Alert createAlert(String type, String message, String icon) {
        WeatherInsightsResponse.Alert alert = new WeatherInsightsResponse.Alert();
        alert.setType(type);
        alert.setMessage(message);
        alert.setIcon(icon);
        return alert;
    }
}
