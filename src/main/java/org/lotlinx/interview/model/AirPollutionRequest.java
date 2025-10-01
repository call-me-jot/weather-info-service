package org.lotlinx.interview.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Request model for air pollution data retrieval. */
public class AirPollutionRequest {

  @JsonProperty("latitude")
  private double latitude;

  @JsonProperty("longitude")
  private double longitude;

  public AirPollutionRequest() {
    // Default constructor for JSON deserialization
  }

  public AirPollutionRequest(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
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

  @Override
  public String toString() {
    return "AirPollutionRequest{" + "latitude=" + latitude + ", longitude=" + longitude + '}';
  }
}
