package org.lotlinx.interview.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Model representing geographical coordinates. */
public class Coordinates {

  @JsonProperty("latitude")
  private double latitude;

  @JsonProperty("longitude")
  private double longitude;

  public Coordinates() {}

  public Coordinates(double latitude, double longitude) {
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
    return "Coordinates{" + "latitude=" + latitude + ", longitude=" + longitude + '}';
  }
}
