package org.lotlinx.interview.config;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration loader that reads from external JSON file or environment variables.
 * Provides fallback to default values if configuration is not found.
 */
public class ConfigLoader {

  private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
  private static final String DEFAULT_CONFIG_FILE = "config.json";
  private static ConfigLoader instance;
  private JsonObject config;

  private ConfigLoader() {
    loadConfiguration();
  }

  /**
   * Gets the singleton instance of ConfigLoader.
   *
   * @return ConfigLoader instance
   */
  public static synchronized ConfigLoader getInstance() {
    if (instance == null) {
      instance = new ConfigLoader();
    }
    return instance;
  }

  /**
   * Loads configuration from file or environment variables.
   */
  private void loadConfiguration() {
    try {
      // Try to load from config file first
      String configFile = System.getProperty("config.file", DEFAULT_CONFIG_FILE);
      Path configPath = Paths.get(configFile);
      
      if (Files.exists(configPath)) {
        String configContent = Files.readString(configPath);
        config = new JsonObject(configContent);
        logger.info("Configuration loaded from file: {}", configPath.toAbsolutePath());
      } else {
        logger.warn("Configuration file not found: {}, using defaults", configPath.toAbsolutePath());
        config = getDefaultConfiguration();
      }
      
      // Override with environment variables if present
      overrideWithEnvironmentVariables();
      
    } catch (IOException e) {
      logger.error("Failed to load configuration file, using defaults", e);
      config = getDefaultConfiguration();
    }
  }

  /**
   * Override configuration values with environment variables if present.
   */
  private void overrideWithEnvironmentVariables() {
    // OpenWeather API Key
    String apiKey = System.getenv("OPENWEATHER_API_KEY");
    if (apiKey != null && !apiKey.trim().isEmpty()) {
      config.getJsonObject("openweather").put("apiKey", apiKey);
      logger.info("OpenWeather API key loaded from environment variable");
    }
    
    // Server configuration
    String serverHost = System.getenv("SERVER_HOST");
    if (serverHost != null && !serverHost.trim().isEmpty()) {
      config.getJsonObject("server").put("host", serverHost);
      logger.info("Server host loaded from environment variable: {}", serverHost);
    }
    
    String serverPort = System.getenv("SERVER_PORT");
    if (serverPort != null && !serverPort.trim().isEmpty()) {
      try {
        int port = Integer.parseInt(serverPort);
        config.getJsonObject("server").put("port", port);
        logger.info("Server port loaded from environment variable: {}", port);
      } catch (NumberFormatException e) {
        logger.warn("Invalid SERVER_PORT environment variable: {}", serverPort);
      }
    }
  }

  /**
   * Gets default configuration values.
   *
   * @return default configuration JsonObject
   */
  private JsonObject getDefaultConfiguration() {
    return new JsonObject()
        .put("openweather", new JsonObject()
            .put("apiKey", "41cf5baac73f77483f69170a1e2d32e6")
            .put("host", "api.openweathermap.org")
            .put("port", 443))
        .put("server", new JsonObject()
            .put("host", "0.0.0.0")
            .put("port", 8080))
        .put("cache", new JsonObject()
            .put("airPollutionTtlMs", 3600000L)
            .put("weatherTtlMs", 600000L)
            .put("geocodingTtlMs", 43200000L))
        .put("circuitBreaker", new JsonObject()
            .put("failureThreshold", 5)
            .put("timeoutMs", 10000L)
            .put("retryTimeoutMs", 60000L))
        .put("rateLimiter", new JsonObject()
            .put("dailyLimit", 1000));
  }

  // OpenWeather Configuration
  public String getOpenWeatherApiKey() {
    return config.getJsonObject("openweather").getString("apiKey");
  }

  public String getOpenWeatherHost() {
    return config.getJsonObject("openweather").getString("host");
  }

  public int getOpenWeatherPort() {
    return config.getJsonObject("openweather").getInteger("port");
  }

  // Server Configuration
  public String getServerHost() {
    return config.getJsonObject("server").getString("host");
  }

  public int getServerPort() {
    return config.getJsonObject("server").getInteger("port");
  }

  // Cache Configuration
  public long getAirPollutionCacheTtl() {
    return config.getJsonObject("cache").getLong("airPollutionTtlMs");
  }

  public long getWeatherCacheTtl() {
    return config.getJsonObject("cache").getLong("weatherTtlMs");
  }

  public long getGeocodingCacheTtl() {
    return config.getJsonObject("cache").getLong("geocodingTtlMs");
  }

  // Circuit Breaker Configuration
  public int getCircuitBreakerFailureThreshold() {
    return config.getJsonObject("circuitBreaker").getInteger("failureThreshold");
  }

  public long getCircuitBreakerTimeoutMs() {
    return config.getJsonObject("circuitBreaker").getLong("timeoutMs");
  }

  public long getCircuitBreakerRetryTimeoutMs() {
    return config.getJsonObject("circuitBreaker").getLong("retryTimeoutMs");
  }

  // Rate Limiter Configuration
  public int getRateLimiterDailyLimit() {
    return config.getJsonObject("rateLimiter").getInteger("dailyLimit");
  }

  /**
   * Gets the full configuration object for debugging.
   *
   * @return configuration JsonObject
   */
  public JsonObject getFullConfig() {
    // Return a copy with masked API key for security
    JsonObject safeCopy = config.copy();
    String apiKey = safeCopy.getJsonObject("openweather").getString("apiKey");
    if (apiKey != null && apiKey.length() > 8) {
      String maskedKey = apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
      safeCopy.getJsonObject("openweather").put("apiKey", maskedKey);
    }
    return safeCopy;
  }
}
