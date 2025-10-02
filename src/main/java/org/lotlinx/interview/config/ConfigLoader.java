package org.lotlinx.interview.config;

import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/** Utility class to load configuration from JSON file or environment variables. */
public class ConfigLoader {
  
  private static final String CONFIG_FILE = "config.json";
  private static JsonObject config;
  
  static {
    loadConfig();
  }
  
  private static void loadConfig() {
    try {
      String configContent = Files.readString(Paths.get(CONFIG_FILE));
      config = new JsonObject(configContent);
      System.out.println("Configuration loaded from " + CONFIG_FILE);
    } catch (IOException e) {
      System.err.println("Warning: Could not load " + CONFIG_FILE + ", using defaults: " + e.getMessage());
      config = getDefaultConfig();
    }
  }
  
  private static JsonObject getDefaultConfig() {
    return new JsonObject()
      .put("server", new JsonObject()
        .put("port", 8080)
        .put("host", "0.0.0.0"))
      .put("openweather", new JsonObject()
        .put("apiKey", "41cf5baac73f77483f69170a1e2d32e6")
        .put("host", "api.openweathermap.org")
        .put("port", 443))
      .put("api", new JsonObject()
        .put("version", "v1")
        .put("endpoints", new JsonObject()
          .put("hello", "/hello")
          .put("airPollution", "/getCurrentAirPollution")
          .put("multiCityWeather", "/api/v1/weather/multi-city")))
      .put("cache", new JsonObject()
        .put("airPollutionTtlMs", 3600000L)
        .put("weatherTtlMs", 600000L)
        .put("geocodingTtlMs", 43200000L));
  }
  
  // Server Configuration
  public static int getServerPort() {
    return Integer.parseInt(System.getenv().getOrDefault("SERVER_PORT", 
      String.valueOf(config.getJsonObject("server").getInteger("port"))));
  }
  
  public static String getServerHost() {
    return System.getenv().getOrDefault("SERVER_HOST", 
      config.getJsonObject("server").getString("host"));
  }
  
  // OpenWeatherMap API Configuration
  public static String getOpenWeatherApiKey() {
    return System.getenv().getOrDefault("OPENWEATHER_API_KEY", 
      config.getJsonObject("openweather").getString("apiKey"));
  }
  
  public static String getOpenWeatherApiHost() {
    return System.getenv().getOrDefault("OPENWEATHER_API_HOST", 
      config.getJsonObject("openweather").getString("host"));
  }
  
  public static int getOpenWeatherApiPort() {
    return Integer.parseInt(System.getenv().getOrDefault("OPENWEATHER_API_PORT", 
      String.valueOf(config.getJsonObject("openweather").getInteger("port"))));
  }
  
  // API Configuration
  public static String getApiVersion() {
    return config.getJsonObject("api").getString("version");
  }
  
  public static String getApiBasePath() {
    return "/api/" + getApiVersion();
  }
  
  public static String getHelloEndpoint() {
    return config.getJsonObject("api").getJsonObject("endpoints").getString("hello");
  }
  
  public static String getAirPollutionEndpoint() {
    return config.getJsonObject("api").getJsonObject("endpoints").getString("airPollution");
  }
  
  public static String getMultiCityWeatherEndpoint() {
    return config.getJsonObject("api").getJsonObject("endpoints").getString("multiCityWeather");
  }
  
  // Cache Configuration
  public static long getAirPollutionCacheTtl() {
    return config.getJsonObject("cache").getLong("airPollutionTtlMs");
  }
  
  public static long getWeatherCacheTtl() {
    return config.getJsonObject("cache").getLong("weatherTtlMs");
  }
  
  public static long getGeocodingCacheTtl() {
    return config.getJsonObject("cache").getLong("geocodingTtlMs");
  }
  
  private ConfigLoader() {
    // Utility class - prevent instantiation
  }
}
