# AQICN API Integration - Complete Guide

## ✅ What's Been Implemented

### Backend Components Created:

1. **Configuration**
   - `AqicnApiConfig.java` - AQICN API configuration
   - Added token and base URL to `application.properties`

2. **DTOs (Data Transfer Objects)**
   - `AqicnResponse.java` - Raw AQICN API response
   - `AqiDetailsResponse.java` - Formatted AQI details
   - `AqicnSearchResponse.java` - Station search results

3. **Service Layer**
   - `AqicnService.java` - Core AQICN integration
     - `getAqiByCity(String city)` - Get AQI by city name
     - `getAqiByCoordinates(Double lat, Double lon)` - Get AQI by coordinates
     - `searchAqiStations(String keyword)` - Search AQI monitoring stations

4. **Controller**
   - `AqiController.java` - REST API endpoints
     - `GET /api/aqi/city?name={city}`
     - `GET /api/aqi/coordinates?lat={lat}&lon={lon}`
     - `GET /api/aqi/search?keyword={keyword}`
     - `GET /api/aqi/health`

5. **Exception Handling**
   - `AqicnApiException.java` - Custom exception
   - Updated `GlobalExceptionHandler.java` to handle AQICN errors

6. **Integration**
   - Updated `WeatherService.java` to merge AQICN data with weather data
   - Weather data from WeatherAPI.com
   - AQI data from AQICN (more accurate, station-based)

---

## 🚀 How to Use

### 1. Start the Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### 2. Test AQICN Endpoints

#### Get AQI by City Name
```bash
curl "http://localhost:8080/api/aqi/city?name=london"
```

**Response:**
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

#### Get AQI by Coordinates
```bash
curl "http://localhost:8080/api/aqi/coordinates?lat=51.5074&lon=-0.1278"
```

#### Search AQI Stations
```bash
curl "http://localhost:8080/api/aqi/search?keyword=london"
```

**Response:**
```json
{
  "status": "ok",
  "data": [
    {
      "uid": 5724,
      "aqi": "55",
      "time": {
        "stime": "2025-11-20 10:00:00"
      },
      "station": {
        "name": "London, United Kingdom",
        "geo": [51.5074, -0.1278]
      }
    }
  ]
}
```

#### Get Current Weather (with AQICN data merged)
```bash
curl "http://localhost:8080/api/weather/current?location=London"
```

This now returns weather data from WeatherAPI.com with AQI data from AQICN!

---

## 🔄 Data Flow

```
User Request → WeatherController
              ↓
         WeatherService
         ↓           ↓
   WeatherAPI    AqicnService
   (weather)     (AQI data)
         ↓           ↓
      Merge Data ←---┘
         ↓
    Return Combined Response
```

---

## 📊 What Data Comes from Where

### From WeatherAPI.com:
- ✅ Temperature (current, feels like)
- ✅ Weather condition (sunny, cloudy, rainy)
- ✅ Humidity
- ✅ Wind speed & direction
- ✅ Pressure
- ✅ Visibility
- ✅ UV Index
- ✅ Precipitation
- ✅ Weather forecasts (3-10 days)
- ✅ Hourly forecasts

### From AQICN:
- ✅ Real-time AQI value
- ✅ PM2.5 levels
- ✅ PM10 levels
- ✅ O3 (Ozone)
- ✅ NO2 (Nitrogen Dioxide)
- ✅ SO2 (Sulfur Dioxide)
- ✅ CO (Carbon Monoxide)
- ✅ Dominant pollutant
- ✅ Station location
- ✅ Health implications
- ✅ AQI category

---

## 🎯 Key Features

### 1. Automatic Data Merging
When you call `/api/weather/current?location=London`, the backend:
1. Fetches weather data from WeatherAPI.com
2. Extracts coordinates from weather response
3. Fetches AQI data from AQICN using those coordinates
4. Merges AQI data into the weather response
5. Returns combined data

### 2. Fallback Mechanism
If AQICN data is unavailable:
- The API still returns weather data
- Logs a warning
- Continues without AQI data

### 3. Caching
All AQICN requests are cached:
- City AQI: 10 minutes
- Coordinate AQI: 10 minutes
- Station search: 10 minutes

### 4. Health Implications
AQICN service automatically calculates:
- AQI category (Good, Moderate, Unhealthy, etc.)
- Health impact message
- Cautionary statements
- Recommendations for outdoor activities

---

## 🔧 Configuration

### application.properties
```properties
# AQICN API Configuration
aqicn.api.token=7498f315cd71cd794d920744c8465d2093324a11
aqicn.api.base-url=https://api.waqi.info

# Cache Configuration
cache.ttl.aqi=600
```

---

## 🧪 Testing Checklist

- [ ] Test AQI by city name
- [ ] Test AQI by coordinates
- [ ] Test station search
- [ ] Test current weather (merged data)
- [ ] Test with invalid city
- [ ] Test with invalid coordinates
- [ ] Test health check endpoints
- [ ] Verify caching works
- [ ] Check error handling

---

## 📝 Frontend Integration

### Update Frontend to Use New Endpoints

```javascript
// Get AQI data separately
const getAqiData = async (city) => {
  const response = await axios.get(`/api/aqi/city?name=${city}`);
  return response.data;
};

// Search AQI stations
const searchStations = async (keyword) => {
  const response = await axios.get(`/api/aqi/search?keyword=${keyword}`);
  return response.data;
};

// Current weather already includes AQICN data
const getCurrentWeather = async (location) => {
  const response = await axios.get(`/api/weather/current?location=${location}`);
  return response.data; // Now has AQICN AQI data!
};
```

---

## 🎨 Display AQI with Color Coding

```javascript
const getAqiColor = (aqi) => {
  if (aqi <= 50) return '#00E400';      // Good - Green
  if (aqi <= 100) return '#FFFF00';     // Moderate - Yellow
  if (aqi <= 150) return '#FF7E00';     // Unhealthy for Sensitive - Orange
  if (aqi <= 200) return '#FF0000';     // Unhealthy - Red
  if (aqi <= 300) return '#8F3F97';     // Very Unhealthy - Purple
  return '#7E0023';                      // Hazardous - Maroon
};
```

---

## 🚨 Error Handling

### Common Errors:

1. **City Not Found**
   - Status: 400
   - Message: "No AQI data received for city: {city}"

2. **Invalid Coordinates**
   - Status: 400
   - Message: "No AQI data received for coordinates"

3. **API Token Invalid**
   - Status: 401
   - Check your token in application.properties

4. **Rate Limit Exceeded**
   - Status: 429
   - AQICN free tier: 1000 requests/minute (very generous)

---

## 📈 Benefits of This Integration

1. **Best of Both Worlds**
   - Comprehensive weather data from WeatherAPI.com
   - Accurate, station-based AQI from AQICN

2. **Real Monitoring Stations**
   - AQICN uses actual air quality sensors
   - More accurate than modeled/estimated data

3. **Health Guidance**
   - Automatic health implications
   - Actionable recommendations
   - Category-based warnings

4. **Flexible Querying**
   - Search by city name
   - Search by coordinates
   - Find nearby stations

5. **Transparent Data Source**
   - Know which station measured the data
   - See exact sensor location
   - Verify data accuracy

---

## 🔮 Next Steps

1. **Deploy Backend**
   - Update Render environment variables with AQICN token
   - Redeploy backend

2. **Update Frontend**
   - Add AQI display components
   - Show station information
   - Display health implications
   - Add color-coded AQI badges

3. **Testing**
   - Test all new endpoints
   - Verify data merging works
   - Check error handling

4. **Documentation**
   - Update API documentation
   - Add AQICN attribution
   - Update user guide

---

## 📚 Resources

- AQICN API Docs: https://aqicn.org/json-api/doc/
- AQICN Website: https://aqicn.org/
- EPA AQI Guide: https://www.airnow.gov/aqi/aqi-basics/

---

## ✨ Success!

You now have a hybrid weather + AQI system that provides:
- Comprehensive weather data
- Accurate, real-time air quality measurements
- Health guidance and recommendations
- Station-based transparency

Your users get the best data from both APIs! 🎉
