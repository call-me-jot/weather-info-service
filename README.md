# Weather Information Service

A robust, production-ready weather information service built with Java 17 and Vert.x. This service provides real-time weather data and air pollution information by integrating with OpenWeatherMap APIs, featuring advanced resilience patterns and performance optimizations.

## Key Highlights

### **Asynchronous & Non-Blocking Architecture**
- Built on Vert.x event-driven framework for high-performance, non-blocking I/O operations
- Fully asynchronous API calls ensure optimal resource utilization and scalability
- Reactive programming patterns with Vert.x Futures for seamless async operations

### **Resilience**
- **Circuit Breaker Pattern**: Automatic fault detection and recovery for external API calls
- **Rate Limiting**: Intelligent request throttling to stay within API quotas (1000 calls/day for free tier)
- **Timeout Management**: Configurable timeouts prevent hanging requests

### **Performance Optimization**
- **In-Memory Caching**: Smart caching with configurable TTL values
  - Geocoding data: 12 hours (location coordinates rarely change)
  - Weather data: 10 minutes (balances freshness with API efficiency)
  - Air pollution data: 1 hour (moderate update frequency)

### **Clean Architecture**
- **Modular Design**: Clear separation of concerns with router → controller → service layers
- **Dependency Injection**: Proper resource management with AutoCloseable implementations
- **Configuration Management**: Externalized configuration with JSON file and environment variable support

### **API Integration**
- **OpenWeatherMap APIs**: Seamless integration with multiple endpoints
  - Current Weather API for real-time weather data
  - Geocoding API for city-to-coordinates conversion
  - Air Pollution API for environmental data
- **API Versioning**: Future-proof endpoint design (`/api/v1/weather/multi-city`)

## Prerequisites

- **Java 17+** - Required for modern language features and performance improvements
- **Maven 3.6+** - For dependency management and build automation
- **OpenWeatherMap API Key** - Free registration at [openweathermap.org](https://openweathermap.org/api)

## Quick Start

### 1. Clone and Setup
```bash
git clone https://github.com/call-me-jot/weather-info-service.git
cd weather-info-service
```

### 2. Configure API Key
Update the API key in `config.json`:
```json
{
  "openweather": {
    "apiKey": "your-api-key-here"
  }
}
```

### 3. Build and Run
```bash
# Using the provided script
./localBuildAndRun.sh

# Or manually with Maven
mvn clean compile
mvn exec:java -Dexec.mainClass="org.lotlinx.interview.MainServerVerticle"
```

The service will start on `http://localhost:8080`

## API Endpoints

### Health Check
```bash
GET /hello
```
**Response:**
```
Hello from Lotlinx!
```

### Air Pollution Data
```bash
GET /getCurrentAirPollution?latitude=43.6534817&longitude=-79.3839347
```
**Response:**
```json
{
  "coord": {
    "longitude": -79.3839,
    "latitude": 43.6535
  },
  "list": [{
    "main": {
      "aqi": 2
    },
    "components": {
      "co": 179.37,
      "no": 0,
      "no2": 0.01,
      "o3": 83.25,
      "so2": 0.16,
      "pm2_5": 3.9,
      "pm10": 10.88,
      "nh3": 0.22
    },
    "dt": 1757607978
  }]
}
```

### Multi-City Weather (New Feature)
```bash
curl --location 'http://localhost:8080/api/v1/weather/multi-city' \
--header 'Content-Type: application/json' \
--data '{"cities": ["London", "Toronto"]}'
```
**Response:**
```json
{
  "totalCities": 3,
  "successfulRequests": 3,
  "failedRequests": 0,
  "weatherData": [
    {
      "city": "Toronto",
      "temperature": 14.74,
      "description": "clear sky",
      "humidity": 47,
      "pressure": 1027.0,
      "windSpeed": 4.82,
      "coordinates": {
        "latitude": 43.6534817,
        "longitude": -79.3839347
      }
    }
  ],
  "failedCities": []
}
```

## Project Structure

```
src/main/java/org/lotlinx/interview/
├── config/                 # Configuration management
│   ├── ApplicationConfig.java
│   ├── ConfigLoader.java
│   └── OpenWeatherConfig.java
├── controller/             # HTTP request handlers
│   └── WeatherController.java
├── model/                  # Data transfer objects
│   ├── WeatherData.java
│   ├── AirPollutionResponse.java
│   └── MultiCityWeatherResponse.java
├── router/                 # Route definitions and middleware
│   └── ApiRouter.java
├── service/                # Business logic layer
│   ├── WeatherService.java
│   └── impl/
│       └── OpenWeatherService.java
├── util/                   # Utility classes and patterns
│   ├── HttpClientUtil.java
│   ├── CircuitBreakerOperation.java
│   ├── RateLimiterOperation.java
│   └── impl/
│       ├── CircuitBreaker.java
│       ├── RateLimiter.java
│       └── InMemoryCache.java
└── MainServerVerticle.java # Application entry point
```

## Testing

The project includes comprehensive test coverage using JUnit 5 and Vert.x TestContext for async-friendly testing.

### Run All Tests
```bash
mvn test
```

## Configuration

The service supports flexible configuration through `config.json`