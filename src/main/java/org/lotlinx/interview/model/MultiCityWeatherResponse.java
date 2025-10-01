package org.lotlinx.interview.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Response model for multi-city weather information. */
public class MultiCityWeatherResponse {

  @JsonProperty("weatherData")
  private List<WeatherData> weatherData;

  @JsonProperty("totalCities")
  private int totalCities;

  @JsonProperty("successfulRequests")
  private int successfulRequests;

  @JsonProperty("failedRequests")
  private int failedRequests;

  @JsonProperty("failedCities")
  private List<String> failedCities;

  public MultiCityWeatherResponse(
      List<WeatherData> weatherData, int totalCities, int successfulRequests, int failedRequests) {
    this.weatherData = weatherData;
    this.totalCities = totalCities;
    this.successfulRequests = successfulRequests;
    this.failedRequests = failedRequests;
    this.failedCities = new java.util.ArrayList<>();
  }

  public MultiCityWeatherResponse(
      List<WeatherData> weatherData, int totalCities, int successfulRequests, int failedRequests, List<String> failedCities) {
    this.weatherData = weatherData;
    this.totalCities = totalCities;
    this.successfulRequests = successfulRequests;
    this.failedRequests = failedRequests;
    this.failedCities = failedCities != null ? failedCities : new java.util.ArrayList<>();
  }

  public List<WeatherData> getWeatherData() {
    return weatherData;
  }

  public void setWeatherData(List<WeatherData> weatherData) {
    this.weatherData = weatherData;
  }

  public int getTotalCities() {
    return totalCities;
  }

  public void setTotalCities(int totalCities) {
    this.totalCities = totalCities;
  }

  public int getSuccessfulRequests() {
    return successfulRequests;
  }

  public void setSuccessfulRequests(int successfulRequests) {
    this.successfulRequests = successfulRequests;
  }

  public int getFailedRequests() {
    return failedRequests;
  }

  public void setFailedRequests(int failedRequests) {
    this.failedRequests = failedRequests;
  }

  public List<String> getFailedCities() {
    return failedCities;
  }

  public void setFailedCities(List<String> failedCities) {
    this.failedCities = failedCities;
  }

  @Override
  public String toString() {
    return "MultiCityWeatherResponse{"
        + "weatherData="
        + weatherData
        + ", totalCities="
        + totalCities
        + ", successfulRequests="
        + successfulRequests
        + ", failedRequests="
        + failedRequests
        + '}';
  }
}
