package org.lotlinx.interview.config;

/** Application configuration constants and settings. */
public class ApplicationConfig {

  // Server Configuration
  public static int getServerPort() {
    return ConfigLoader.getServerPort();
  }
  
  public static String getServerHost() {
    return ConfigLoader.getServerHost();
  }

  // OpenWeatherMap API Configuration
  public static String getOpenWeatherApiHost() {
    return ConfigLoader.getOpenWeatherApiHost();
  }
  
  public static String getOpenWeatherApiKey() {
    return ConfigLoader.getOpenWeatherApiKey();
  }
  
  public static int getOpenWeatherApiPort() {
    return ConfigLoader.getOpenWeatherApiPort();
  }
  
  // OpenWeatherMap API Paths
  public static final String AIR_POLLUTION_API_PATH = "/data/2.5/air_pollution";
  public static final String GEOCODING_API_PATH = "/geo/1.0/direct";
  public static final String CURRENT_WEATHER_API_PATH = "/data/2.5/weather";

  // Application Settings
  public static final int GLOBAL_SLEEP_TIME_MS = 500;
  public static final int HTTP_TIMEOUT_MS = 10000;

  // API Versioning
  public static String getApiVersion() {
    return ConfigLoader.getApiVersion();
  }
  
  public static String getApiBasePath() {
    return ConfigLoader.getApiBasePath();
  }

  // API Endpoints
  public static String getHelloEndpoint() {
    return ConfigLoader.getHelloEndpoint();
  }
  
  public static String getAirPollutionEndpoint() {
    return ConfigLoader.getAirPollutionEndpoint();
  }
  
  public static String getMultiCityWeatherEndpoint() {
    return ConfigLoader.getMultiCityWeatherEndpoint();
  }

  // Content Types
  public static final String CONTENT_TYPE_JSON = "application/json";
  public static final String CONTENT_TYPE_TEXT = "text/plain";

  // Cache Configuration (in milliseconds)
  public static long getAirPollutionCacheTtl() {
    return ConfigLoader.getAirPollutionCacheTtl();
  }
  
  public static long getWeatherCacheTtl() {
    return ConfigLoader.getWeatherCacheTtl();
  }
  
  public static long getGeocodingCacheTtl() {
    return ConfigLoader.getGeocodingCacheTtl();
  }

  private ApplicationConfig() {
    // Utility class - prevent instantiation
  }
}
