package org.lotlinx.interview.config;

/** Application configuration constants and settings. */
public class ApplicationConfig {

  private static final ConfigLoader configLoader = ConfigLoader.getInstance();

  // Server Configuration
  public static int getServerPort() {
    return configLoader.getServerPort();
  }

  public static String getServerHost() {
    return configLoader.getServerHost();
  }

  // OpenWeatherMap API Configuration
  public static String getOpenWeatherApiHost() {
    return configLoader.getOpenWeatherHost();
  }

  public static String getOpenWeatherApiKey() {
    return configLoader.getOpenWeatherApiKey();
  }

  public static int getOpenWeatherApiPort() {
    return configLoader.getOpenWeatherPort();
  }
  
  // OpenWeatherMap API Paths
  public static final String AIR_POLLUTION_API_PATH = "/data/2.5/air_pollution";
  public static final String GEOCODING_API_PATH = "/geo/1.0/direct";
  public static final String CURRENT_WEATHER_API_PATH = "/data/2.5/weather";

  // Application Settings
  public static final int GLOBAL_SLEEP_TIME_MS = 500;
  public static final int HTTP_TIMEOUT_MS = 10000;

  // API Versioning
  public static final String API_VERSION = "v1";
  public static final String API_BASE_PATH = "/api/" + API_VERSION;

  // API Endpoints
  public static final String HELLO_ENDPOINT = "/hello";
  public static final String AIR_POLLUTION_ENDPOINT = "/getCurrentAirPollution";
  public static final String MULTI_CITY_WEATHER_ENDPOINT = API_BASE_PATH + "/weather/multi-city";

  // Content Types
  public static final String CONTENT_TYPE_JSON = "application/json";
  public static final String CONTENT_TYPE_TEXT = "text/plain";

  // Cache Configuration (in milliseconds)
  public static long getAirPollutionCacheTtlMs() {
    return configLoader.getAirPollutionCacheTtl();
  }

  public static long getWeatherCacheTtlMs() {
    return configLoader.getWeatherCacheTtl();
  }

  public static long getGeocodingCacheTtlMs() {
    return configLoader.getGeocodingCacheTtl();
  }

  private ApplicationConfig() {
    // Utility class - prevent instantiation
  }
}
