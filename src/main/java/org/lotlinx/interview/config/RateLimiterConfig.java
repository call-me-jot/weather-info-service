package org.lotlinx.interview.config;

/**
 * Configuration for rate limiting settings.
 * Centralizes rate limiting parameters for easy tuning.
 */
public class RateLimiterConfig {

  // OpenWeatherMap API rate limiting settings
  public static final String GEOCODING_RATE_LIMITER_NAME = "geocoding-api";
  public static final String WEATHER_RATE_LIMITER_NAME = "weather-api";
  public static final String AIR_POLLUTION_RATE_LIMITER_NAME = "air-pollution-api";

  // Free tier limits (per day)
  public static final int FREE_TIER_DAILY_LIMIT = 1000;

  private RateLimiterConfig() {
    // Utility class
  }
}
