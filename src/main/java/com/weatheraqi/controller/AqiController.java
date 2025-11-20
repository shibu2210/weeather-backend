package com.weatheraqi.controller;

import com.weatheraqi.dto.AqicnSearchResponse;
import com.weatheraqi.dto.AqiDetailsResponse;
import com.weatheraqi.service.AqicnService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/aqi")
@RequiredArgsConstructor
@Validated
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AqiController {
    
    private final AqicnService aqicnService;
    
    @GetMapping("/city")
    public ResponseEntity<AqiDetailsResponse> getAqiByCity(
            @RequestParam @NotBlank(message = "City name is required") String name) {
        log.info("GET /api/aqi/city - name: {}", name);
        AqiDetailsResponse response = aqicnService.getAqiByCity(name);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/coordinates")
    public ResponseEntity<AqiDetailsResponse> getAqiByCoordinates(
            @RequestParam @NotNull(message = "Latitude is required") Double lat,
            @RequestParam @NotNull(message = "Longitude is required") Double lon) {
        log.info("GET /api/aqi/coordinates - lat: {}, lon: {}", lat, lon);
        AqiDetailsResponse response = aqicnService.getAqiByCoordinates(lat, lon);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/station")
    public ResponseEntity<AqiDetailsResponse> getAqiByStationUid(
            @RequestParam @NotNull(message = "Station UID is required") Integer uid) {
        log.info("GET /api/aqi/station - uid: {}", uid);
        AqiDetailsResponse response = aqicnService.getAqiByStationUid(uid);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    public ResponseEntity<AqicnSearchResponse> searchStations(
            @RequestParam @NotBlank(message = "Keyword is required") String keyword) {
        log.info("GET /api/aqi/search - keyword: {}", keyword);
        AqicnSearchResponse response = aqicnService.searchAqiStations(keyword);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AQI API is running");
    }
}
