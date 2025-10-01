package org.lotlinx.interview.config;

/** Configuration for OpenWeatherMap API integration. */
public class OpenWeatherConfig {

  private final String apiKey;
  private final String host;
  private final String path;
  private final int port;
  private final int timeoutMs;

  public OpenWeatherConfig() {
    this.apiKey = ApplicationConfig.OPENWEATHER_API_KEY;
    this.host = ApplicationConfig.OPENWEATHER_API_HOST;
    this.path = ApplicationConfig.AIR_POLLUTION_API_PATH;
    this.port = ApplicationConfig.OPENWEATHER_API_PORT;
    this.timeoutMs = ApplicationConfig.HTTP_TIMEOUT_MS;
  }

  public OpenWeatherConfig(String apiKey, String host, String path, int port, int timeoutMs) {
    this.apiKey = apiKey;
    this.host = host;
    this.path = path;
    this.port = port;
    this.timeoutMs = timeoutMs;
  }

  public String getApiKey() {
    return apiKey;
  }

  public String getHost() {
    return host;
  }

  public String getPath() {
    return path;
  }

  public int getPort() {
    return port;
  }

  /** Creates a configuration for the Geocoding API. */
  public static OpenWeatherConfig forGeocoding() {
    return new OpenWeatherConfig(
        ApplicationConfig.OPENWEATHER_API_KEY,
        ApplicationConfig.OPENWEATHER_API_HOST,
        ApplicationConfig.GEOCODING_API_PATH,
        ApplicationConfig.OPENWEATHER_API_PORT,
        ApplicationConfig.HTTP_TIMEOUT_MS);
  }

  /** Creates a configuration for the Current Weather API. */
  public static OpenWeatherConfig forCurrentWeather() {
    return new OpenWeatherConfig(
        ApplicationConfig.OPENWEATHER_API_KEY,
        ApplicationConfig.OPENWEATHER_API_HOST,
        ApplicationConfig.CURRENT_WEATHER_API_PATH,
        ApplicationConfig.OPENWEATHER_API_PORT,
        ApplicationConfig.HTTP_TIMEOUT_MS);
  }
}
