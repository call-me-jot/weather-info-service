package org.lotlinx.interview.config;

/**
 * Configuration for circuit breaker settings.
 * Centralizes circuit breaker parameters for easy tuning.
 */
public class CircuitBreakerConfig {

  // OpenWeatherMap API circuit breaker settings
  public static final String GEOCODING_CIRCUIT_BREAKER_NAME = "geocoding-api";
  public static final String WEATHER_CIRCUIT_BREAKER_NAME = "weather-api";
  public static final String AIR_POLLUTION_CIRCUIT_BREAKER_NAME = "air-pollution-api";

  // Failure threshold: number of consecutive failures before opening circuit
  public static final int FAILURE_THRESHOLD = 5;

  // Timeout for individual API calls (in milliseconds)
  public static final long API_TIMEOUT_MS = 10000; // 10 seconds

  // Retry timeout: how long to wait before attempting to close circuit (in milliseconds)
  public static final long RETRY_TIMEOUT_MS = 60000; // 1 minute

  // Half-open success threshold: number of successful calls needed to close circuit
  public static final int HALF_OPEN_SUCCESS_THRESHOLD = 3;

  private CircuitBreakerConfig() {
    // Utility class
  }
}
