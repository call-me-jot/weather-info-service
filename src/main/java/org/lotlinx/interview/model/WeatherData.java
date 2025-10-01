package org.lotlinx.interview.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Model representing weather data for a single city. */
public class WeatherData {

  @JsonProperty("city")
  private String city;

  @JsonProperty("temperature")
  private double temperature;

  @JsonProperty("description")
  private String description;

  @JsonProperty("humidity")
  private int humidity;

  @JsonProperty("pressure")
  private double pressure;

  @JsonProperty("windSpeed")
  private double windSpeed;

  @JsonProperty("coordinates")
  private Coordinates coordinates;

  public WeatherData() {}

  public WeatherData(
      String city,
      double temperature,
      String description,
      int humidity,
      double pressure,
      double windSpeed,
      Coordinates coordinates) {
    this.city = city;
    this.temperature = temperature;
    this.description = description;
    this.humidity = humidity;
    this.pressure = pressure;
    this.windSpeed = windSpeed;
    this.coordinates = coordinates;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public double getTemperature() {
    return temperature;
  }

  public void setTemperature(double temperature) {
    this.temperature = temperature;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getHumidity() {
    return humidity;
  }

  public void setHumidity(int humidity) {
    this.humidity = humidity;
  }

  public double getPressure() {
    return pressure;
  }

  public void setPressure(double pressure) {
    this.pressure = pressure;
  }

  public double getWindSpeed() {
    return windSpeed;
  }

  public void setWindSpeed(double windSpeed) {
    this.windSpeed = windSpeed;
  }

  public Coordinates getCoordinates() {
    return coordinates;
  }

  public void setCoordinates(Coordinates coordinates) {
    this.coordinates = coordinates;
  }

  @Override
  public String toString() {
    return "WeatherData{"
        + "city='"
        + city
        + '\''
        + ", temperature="
        + temperature
        + ", description='"
        + description
        + '\''
        + ", humidity="
        + humidity
        + ", pressure="
        + pressure
        + ", windSpeed="
        + windSpeed
        + ", coordinates="
        + coordinates
        + '}';
  }
}
