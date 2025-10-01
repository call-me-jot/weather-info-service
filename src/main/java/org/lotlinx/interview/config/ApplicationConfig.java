package org.lotlinx.interview.config;

/** Application configuration constants and settings. */
public class ApplicationConfig {

  // Server Configuration
  public static final int SERVER_PORT = 8080;
  public static final String SERVER_HOST = "0.0.0.0";

  // OpenWeatherMap API Configuration
  public static final String OPENWEATHER_API_HOST = "api.openweathermap.org";
  public static final String OPENWEATHER_API_PATH = "/data/2.5/air_pollution";
  public static final String OPENWEATHER_API_KEY = "a915d8a317793b5c54fe6d96a834e33c";
  public static final int OPENWEATHER_API_PORT = 443;

  // Application Settings
  public static final int GLOBAL_SLEEP_TIME_MS = 500;
  public static final int HTTP_TIMEOUT_MS = 10000;

  // API Endpoints
  public static final String HELLO_ENDPOINT = "/hello";
  public static final String AIR_POLLUTION_ENDPOINT = "/getCurrentAirPollution";

  // Content Types
  public static final String CONTENT_TYPE_JSON = "application/json";
  public static final String CONTENT_TYPE_TEXT = "text/plain";

  private ApplicationConfig() {
    // Utility class - prevent instantiation
  }
}
