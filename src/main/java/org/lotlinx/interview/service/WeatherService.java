package org.lotlinx.interview.service;

import io.vertx.core.Future;
import org.lotlinx.interview.model.AirPollutionResponse;
import org.lotlinx.interview.model.MultiCityWeatherResponse;

/** Service interface for weather-related operations. */
public interface WeatherService {

  /**
   * Retrieves current air pollution data for the given coordinates.
   *
   * @param latitude the latitude coordinate
   * @param longitude the longitude coordinate
   * @return Future containing the air pollution response
   */
  Future<AirPollutionResponse> getCurrentAirPollution(double latitude, double longitude);

  /**
   * Retrieves current weather data for multiple cities.
   *
   * @param cities list of city names
   * @return Future containing the multi-city weather response
   */
  Future<MultiCityWeatherResponse> getMultiCityWeather(java.util.List<String> cities);
}
