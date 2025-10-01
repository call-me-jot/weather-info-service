package org.lotlinx.interview.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Response model for air pollution data from OpenWeatherMap API. */
public class AirPollutionResponse {

  @JsonProperty("coord")
  private Coordinates coord;

  @JsonProperty("list")
  private List<AirPollutionData> list;

  public AirPollutionResponse() {
    // Default constructor for JSON deserialization
  }

  public Coordinates getCoord() {
    return coord;
  }

  public void setCoord(Coordinates coord) {
    this.coord = coord;
  }

  public List<AirPollutionData> getList() {
    return list;
  }

  public void setList(List<AirPollutionData> list) {
    this.list = list;
  }

  public static class Coordinates {
    @JsonProperty("lon")
    private double longitude;

    @JsonProperty("lat")
    private double latitude;

    public Coordinates() {}

    public Coordinates(double longitude, double latitude) {
      this.longitude = longitude;
      this.latitude = latitude;
    }

    public double getLongitude() {
      return longitude;
    }

    public void setLongitude(double longitude) {
      this.longitude = longitude;
    }

    public double getLatitude() {
      return latitude;
    }

    public void setLatitude(double latitude) {
      this.latitude = latitude;
    }
  }

  public static class AirPollutionData {
    @JsonProperty("main")
    private Main main;

    @JsonProperty("components")
    private Components components;

    @JsonProperty("dt")
    private long timestamp;

    public AirPollutionData() {}

    public Main getMain() {
      return main;
    }

    public void setMain(Main main) {
      this.main = main;
    }

    public Components getComponents() {
      return components;
    }

    public void setComponents(Components components) {
      this.components = components;
    }

    public long getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
    }
  }

  public static class Main {
    @JsonProperty("aqi")
    private int aqi;

    public Main() {}

    public Main(int aqi) {
      this.aqi = aqi;
    }

    public int getAqi() {
      return aqi;
    }

    public void setAqi(int aqi) {
      this.aqi = aqi;
    }
  }

  public static class Components {
    @JsonProperty("co")
    private double co;

    @JsonProperty("no")
    private double no;

    @JsonProperty("no2")
    private double no2;

    @JsonProperty("o3")
    private double o3;

    @JsonProperty("so2")
    private double so2;

    @JsonProperty("pm2_5")
    private double pm25;

    @JsonProperty("pm10")
    private double pm10;

    @JsonProperty("nh3")
    private double nh3;

    public Components() {}

    // Getters and setters
    public double getCo() {
      return co;
    }

    public void setCo(double co) {
      this.co = co;
    }

    public double getNo() {
      return no;
    }

    public void setNo(double no) {
      this.no = no;
    }

    public double getNo2() {
      return no2;
    }

    public void setNo2(double no2) {
      this.no2 = no2;
    }

    public double getO3() {
      return o3;
    }

    public void setO3(double o3) {
      this.o3 = o3;
    }

    public double getSo2() {
      return so2;
    }

    public void setSo2(double so2) {
      this.so2 = so2;
    }

    public double getPm25() {
      return pm25;
    }

    public void setPm25(double pm25) {
      this.pm25 = pm25;
    }

    public double getPm10() {
      return pm10;
    }

    public void setPm10(double pm10) {
      this.pm10 = pm10;
    }

    public double getNh3() {
      return nh3;
    }

    public void setNh3(double nh3) {
      this.nh3 = nh3;
    }
  }
}
