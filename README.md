# Weather AQI Backend

Spring Boot REST API that provides weather and air quality data by integrating with external APIs.

## Tech Stack

- **Spring Boot 3.2.0** - Application framework
- **Java 17** - Programming language
- **Maven** - Build tool
- **Spring Cache** - Response caching
- **RestTemplate** - HTTP client

## Features

- 🌐 RESTful API endpoints
- 🔄 Integration with Open-Meteo (free, no API key) and AQICN.org
- 💾 Response caching for improved performance
- 🛡️ CORS configuration for frontend integration
- ⚠️ Global exception handling
- 🏥 Health check endpoint
- 📅 Up to 16-day weather forecast support

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- AQICN.org API token (for air quality data)

## Setup

1. Configure API token in `src/main/resources/application.properties`:
```properties
# Open-Meteo requires no API key!
aqicn.api.token=YOUR_AQICN_TOKEN
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The API will start on `http://localhost:8080`

## API Endpoints

### Weather Endpoints

- `GET /api/weather/current?location={location}`
  - Get current weather for a location
  - Returns: Current weather data with temperature, conditions, etc.

- `GET /api/weather/forecast?location={location}&days={days}`
  - Get weather forecast (1-16 days, default 7)
  - Returns: Forecast data with hourly and daily predictions

- `GET /api/weather/search?query={query}`
  - Search for locations
  - Returns: List of matching locations with coordinates

### AQI Endpoints

- `GET /api/aqi/current?location={location}`
  - Get current air quality index for a location
  - Returns: AQI value and pollutant details

### Health Check

- `GET /api/weather/health`
  - Check API health status
  - Returns: Status message

## Project Structure

```
src/main/java/com/weatheraqi/
├── config/          # Configuration classes
│   ├── CacheConfig.java
│   └── WebConfig.java
├── controller/      # REST controllers
│   ├── WeatherController.java
│   └── AqiController.java
├── dto/             # Data Transfer Objects
│   ├── CurrentWeatherResponse.java
│   ├── ForecastResponse.java
│   └── AqiDetailsResponse.java
├── exception/       # Exception handling
│   └── GlobalExceptionHandler.java
├── service/         # Business logic
│   ├── WeatherService.java
│   └── AqicnService.java
└── WeatherAqiApplication.java
```

## Configuration

### Application Properties

Key configurations in `application.properties`:

```properties
# Server
server.port=8080

# Open-Meteo API (No key required!)
openmeteo.base-url=https://api.open-meteo.com/v1
openmeteo.geocoding-url=https://geocoding-api.open-meteo.com/v1

# AQI API
aqicn.api.token=YOUR_TOKEN
aqicn.api.base-url=https://api.waqi.info

# CORS
cors.allowed-origins=http://localhost:3000,https://your-frontend-domain.com

# Cache
spring.cache.type=simple
```

## Building for Production

```bash
mvn clean package
```

Run the JAR file:
```bash
java -jar target/weather-aqi-backend-1.0.0.jar
```

## Docker Support

Build Docker image:
```bash
docker build -t weather-aqi-backend .
```

Run container:
```bash
docker run -p 8080:8080 weather-aqi-backend
```

## Error Handling

The API includes global exception handling for:
- Invalid location queries
- API rate limits
- Network errors
- Invalid request parameters

All errors return appropriate HTTP status codes with descriptive messages.

## Caching

Response caching is enabled for:
- Current weather data (5 minutes)
- Forecast data (30 minutes)
- Location search results (1 hour)

## API Rate Limits

Be aware of rate limits from external APIs:
- Open-Meteo: Free with no API key, reasonable use limits (10,000 requests/day)
- AQICN.org: Typically 1000 requests/day for free tier

## Why Open-Meteo?

- ✅ Completely free, no API key required
- ✅ Up to 16-day forecast (vs 3 days on WeatherAPI free tier)
- ✅ No rate limits for reasonable use
- ✅ High-quality data from national weather services
- ✅ Open-source and community-driven
