# AQICN Integration - Quick Test Guide

## ✅ Build Successful!

Your backend now integrates AQICN API for accurate air quality data.

---

## 🚀 Start the Backend

```bash
cd backend
mvn spring-boot:run
```

Wait for: `Started WeatherAqiBackendApplication`

---

## 🧪 Test the New Endpoints

### 1. Test AQICN Health Check
```bash
curl http://localhost:8080/api/aqi/health
```
**Expected:** `"AQI API is running"`

---

### 2. Get AQI for London
```bash
curl "http://localhost:8080/api/aqi/city?name=london"
```

**Expected Response:**
```json
{
  "aqi": 55,
  "category": "Moderate",
  "dominantPollutant": "pm25",
  "stationName": "London",
  "latitude": 51.5074,
  "longitude": -0.1278,
  "lastUpdated": "2025-11-20 10:00:00",
  "pollutants": {
    "pm2_5": 55.0,
    "pm10": 45.0,
    "o3": 12.0,
    "no2": 25.0,
    "so2": 3.0,
    "co": 0.3
  },
  "healthImplications": {
    "level": "Moderate",
    "message": "Air quality is acceptable...",
    "cautionaryStatement": "Unusually sensitive people should consider limiting prolonged outdoor exertion."
  }
}
```

---

### 3. Get AQI by Coordinates (New York)
```bash
curl "http://localhost:8080/api/aqi/coordinates?lat=40.7128&lon=-74.0060"
```

---

### 4. Search AQI Stations
```bash
curl "http://localhost:8080/api/aqi/search?keyword=tokyo"
```

**Expected:** List of AQI monitoring stations in Tokyo

---

### 5. Get Current Weather (Now with AQICN Data!)
```bash
curl "http://localhost:8080/api/weather/current?location=London"
```

**What's Different:**
- Weather data from WeatherAPI.com
- AQI data from AQICN (more accurate!)
- Merged automatically

---

## 🎯 Test in Browser

Open these URLs in your browser:

1. **AQI Health:** http://localhost:8080/api/aqi/health
2. **London AQI:** http://localhost:8080/api/aqi/city?name=london
3. **New York AQI:** http://localhost:8080/api/aqi/city?name=newyork
4. **Tokyo AQI:** http://localhost:8080/api/aqi/city?name=tokyo
5. **Search Stations:** http://localhost:8080/api/aqi/search?keyword=paris
6. **Weather + AQI:** http://localhost:8080/api/weather/current?location=London

---

## 📊 What You'll See

### AQI Categories:
- **0-50:** Good (Green)
- **51-100:** Moderate (Yellow)
- **101-150:** Unhealthy for Sensitive Groups (Orange)
- **151-200:** Unhealthy (Red)
- **201-300:** Very Unhealthy (Purple)
- **301+:** Hazardous (Maroon)

### Pollutants Measured:
- **PM2.5:** Fine particulate matter
- **PM10:** Coarse particulate matter
- **O3:** Ozone
- **NO2:** Nitrogen dioxide
- **SO2:** Sulfur dioxide
- **CO:** Carbon monoxide

---

## 🔍 Verify Integration

### Check Logs:
When you call `/api/weather/current?location=London`, you should see:

```
Fetching current weather for location: London
Fetching AQICN data for coordinates: 51.52, -0.11
Successfully merged AQICN data
```

This confirms AQICN data is being fetched and merged!

---

## ✨ Key Features

1. **Automatic Merging:** Weather + AQI data combined automatically
2. **Fallback:** If AQICN fails, weather data still works
3. **Caching:** AQI data cached for 10 minutes
4. **Health Guidance:** Automatic health implications
5. **Station Info:** Know where the data comes from

---

## 🐛 Troubleshooting

### "No AQI data received"
- Check if the city has an AQI monitoring station
- Try coordinates instead: `/api/aqi/coordinates?lat=X&lon=Y`
- Search for nearby stations: `/api/aqi/search?keyword=city`

### "Failed to fetch AQI data"
- Verify AQICN token in `application.properties`
- Check internet connection
- Try a different city

### Weather works but no AQI
- Check logs for AQICN errors
- AQI data is optional - weather still works
- Some locations may not have AQI stations

---

## 🎉 Success Indicators

✅ Backend compiles without errors
✅ All endpoints return 200 status
✅ AQI data includes pollutant levels
✅ Health implications are generated
✅ Weather endpoint includes AQI data
✅ Logs show "Successfully merged AQICN data"

---

## 📝 Next Steps

1. **Deploy to Render:**
   - Add `AQICN_API_TOKEN` environment variable
   - Redeploy backend

2. **Update Frontend:**
   - Display AQI with color coding
   - Show health implications
   - Add station information

3. **Test Production:**
   - Verify AQICN integration works on Render
   - Test with various cities
   - Check error handling

---

## 🔗 API Endpoints Summary

| Endpoint | Purpose | Example |
|----------|---------|---------|
| `/api/aqi/city` | Get AQI by city name | `?name=london` |
| `/api/aqi/coordinates` | Get AQI by lat/lon | `?lat=51.5&lon=-0.1` |
| `/api/aqi/search` | Search AQI stations | `?keyword=tokyo` |
| `/api/weather/current` | Weather + AQI merged | `?location=London` |
| `/api/weather/forecast` | Weather forecast | `?location=London&days=3` |

---

## 🎊 You're All Set!

Your backend now uses:
- **WeatherAPI.com** for weather data
- **AQICN** for accurate air quality data

Best of both worlds! 🌤️ + 🌫️ = 🎯
