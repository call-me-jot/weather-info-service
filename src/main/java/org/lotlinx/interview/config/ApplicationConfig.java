package org.lotlinx.interview.config;

/** Application configuration constants and settings. */
public class ApplicationConfig {

  // Server Configuration
  public static final int SERVER_PORT = 8080;
  public static final String SERVER_HOST = "0.0.0.0";

  // OpenWeatherMap API Configuration
  public static final String OPENWEATHER_API_HOST = "api.openweathermap.org";
  public static final String OPENWEATHER_API_KEY = "41cf5baac73f77483f69170a1e2d32e6";
  public static final int OPENWEATHER_API_PORT = 443;
  
  // OpenWeatherMap API Paths
  public static final String AIR_POLLUTION_API_PATH = "/data/2.5/air_pollution";
  public static final String GEOCODING_API_PATH = "/geo/1.0/direct";
  public static final String CURRENT_WEATHER_API_PATH = "/data/2.5/weather";

  // Application Settings
  public static final int GLOBAL_SLEEP_TIME_MS = 500;
  public static final int HTTP_TIMEOUT_MS = 10000;

  // API Endpoints
  public static final String HELLO_ENDPOINT = "/hello";
  public static final String AIR_POLLUTION_ENDPOINT = "/getCurrentAirPollution";
  public static final String MULTI_CITY_WEATHER_ENDPOINT = "/getMultiCityWeather";

  // Content Types
  public static final String CONTENT_TYPE_JSON = "application/json";
  public static final String CONTENT_TYPE_TEXT = "text/plain";

  private ApplicationConfig() {
    // Utility class - prevent instantiation
  }
}
