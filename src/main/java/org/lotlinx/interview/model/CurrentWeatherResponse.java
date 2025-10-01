package org.lotlinx.interview.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Response model for OpenWeatherMap Current Weather API. */
public class CurrentWeatherResponse {

  @JsonProperty("name")
  private String cityName;

  @JsonProperty("main")
  private MainWeatherData main;

  @JsonProperty("weather")
  private List<Weather> weather;

  @JsonProperty("wind")
  private Wind wind;

  @JsonProperty("coord")
  private Coordinates coord;

  public CurrentWeatherResponse() {}

  public String getCityName() {
    return cityName;
  }

  public void setCityName(String cityName) {
    this.cityName = cityName;
  }

  public MainWeatherData getMain() {
    return main;
  }

  public void setMain(MainWeatherData main) {
    this.main = main;
  }

  public List<Weather> getWeather() {
    return weather;
  }

  public void setWeather(List<Weather> weather) {
    this.weather = weather;
  }

  public Wind getWind() {
    return wind;
  }

  public void setWind(Wind wind) {
    this.wind = wind;
  }

  public Coordinates getCoord() {
    return coord;
  }

  public void setCoord(Coordinates coord) {
    this.coord = coord;
  }

  @Override
  public String toString() {
    return "CurrentWeatherResponse{"
        + "cityName='"
        + cityName
        + '\''
        + ", main="
        + main
        + ", weather="
        + weather
        + ", wind="
        + wind
        + ", coord="
        + coord
        + '}';
  }

  /** Inner class for main weather data. */
  public static class MainWeatherData {
    @JsonProperty("temp")
    private double temperature;

    @JsonProperty("humidity")
    private int humidity;

    @JsonProperty("pressure")
    private double pressure;

    public MainWeatherData() {}

    public double getTemperature() {
      return temperature;
    }

    public void setTemperature(double temperature) {
      this.temperature = temperature;
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

    @Override
    public String toString() {
      return "MainWeatherData{"
          + "temperature="
          + temperature
          + ", humidity="
          + humidity
          + ", pressure="
          + pressure
          + '}';
    }
  }

  /** Inner class for weather description. */
  public static class Weather {
    @JsonProperty("description")
    private String description;

    public Weather() {}

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    @Override
    public String toString() {
      return "Weather{" + "description='" + description + '\'' + '}';
    }
  }

  /** Inner class for wind data. */
  public static class Wind {
    @JsonProperty("speed")
    private double speed;

    public Wind() {}

    public double getSpeed() {
      return speed;
    }

    public void setSpeed(double speed) {
      this.speed = speed;
    }

    @Override
    public String toString() {
      return "Wind{" + "speed=" + speed + '}';
    }
  }
}
