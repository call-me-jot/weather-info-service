package org.lotlinx.interview.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Response model for OpenWeatherMap Geocoding API. */
public class GeocodingResponse {

  @JsonProperty("name")
  private String name;

  @JsonProperty("lat")
  private double latitude;

  @JsonProperty("lon")
  private double longitude;

  @JsonProperty("country")
  private String country;

  @JsonProperty("state")
  private String state;

  public GeocodingResponse() {}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  @Override
  public String toString() {
    return "GeocodingResponse{"
        + "name='"
        + name
        + '\''
        + ", latitude="
        + latitude
        + ", longitude="
        + longitude
        + ", country='"
        + country
        + '\''
        + ", state='"
        + state
        + '\''
        + '}';
  }
}
