package org.lotlinx.interview.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Standard error response model for API errors. */
public class ApiError {

  @JsonProperty("error")
  private String error;

  @JsonProperty("message")
  private String message;

  @JsonProperty("timestamp")
  private long timestamp;

  public ApiError() {
    this.timestamp = System.currentTimeMillis();
  }

  public ApiError(String error, String message) {
    this();
    this.error = error;
    this.message = message;
  }

  public ApiError(String error) {
    this();
    this.error = error;
    this.message = error;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
