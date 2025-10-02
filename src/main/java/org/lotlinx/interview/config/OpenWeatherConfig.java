package org.lotlinx.interview.config;

/** Configuration for OpenWeatherMap API integration. */
public class OpenWeatherConfig {

  private final String apiKey;
  private final String host;
  private final String path;
  private final int port;
  private final int timeoutMs;

  public OpenWeatherConfig() {
    this.apiKey = ApplicationConfig.getOpenWeatherApiKey();
    this.host = ApplicationConfig.getOpenWeatherApiHost();
    this.path = ApplicationConfig.AIR_POLLUTION_API_PATH;
    this.port = ApplicationConfig.getOpenWeatherApiPort();
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
        ApplicationConfig.getOpenWeatherApiKey(),
        ApplicationConfig.getOpenWeatherApiHost(),
        ApplicationConfig.GEOCODING_API_PATH,
        ApplicationConfig.getOpenWeatherApiPort(),
        ApplicationConfig.HTTP_TIMEOUT_MS);
  }

  /** Creates a configuration for the Current Weather API. */
  public static OpenWeatherConfig forCurrentWeather() {
    return new OpenWeatherConfig(
        ApplicationConfig.getOpenWeatherApiKey(),
        ApplicationConfig.getOpenWeatherApiHost(),
        ApplicationConfig.CURRENT_WEATHER_API_PATH,
        ApplicationConfig.getOpenWeatherApiPort(),
        ApplicationConfig.HTTP_TIMEOUT_MS);
  }
}
