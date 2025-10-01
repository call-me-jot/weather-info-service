package org.lotlinx.interview.controller;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.ArrayList;
import java.util.List;
import org.lotlinx.interview.model.ApiError;
import org.lotlinx.interview.model.MultiCityWeatherResponse;
import org.lotlinx.interview.model.WeatherData;
import org.lotlinx.interview.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Controller for handling weather-related HTTP requests. */
public class WeatherController {

  private static final Logger logger = LoggerFactory.getLogger(WeatherController.class);

  private final WeatherService weatherService;

  public WeatherController(WeatherService weatherService) {
    this.weatherService = weatherService;
  }

  /** Handles the hello endpoint request. */
  public void handleHello(RoutingContext context) {
    logger.info("Handler received request: {}", context.request().path());

    HttpServerResponse response = context.response();
    response.putHeader("content-type", "text/plain");
    response.end("Hello from Lotlinx!");
  }

  /** Handles the air pollution endpoint request. */
  public void handleAirPollution(RoutingContext context) {
    logger.info(
        "Handler received request: {} with Params: {}",
        context.request().path(),
        context.request().params());

    try {
      double latitude = getQueryParameterDouble("latitude", context);
      double longitude = getQueryParameterDouble("longitude", context);

      // Validate coordinates
      String validationError = validateAirPollutionRequest(latitude, longitude);
      if (validationError != null) {
        sendErrorResponse(context, 400, validationError);
        return;
      }

      weatherService
          .getCurrentAirPollution(latitude, longitude)
          .onComplete(
              ar -> {
                if (ar.succeeded()) {
                  logger.info("Successfully processed air pollution request");
                  sendSuccessResponse(context, ar.result());
                } else {
                  logger.error("Error while processing air pollution request", ar.cause());
                  sendErrorResponse(
                      context, 500, "Internal server error: " + ar.cause().getMessage());
                }
              });

    } catch (Exception e) {
      logger.error("Unexpected error in air pollution handler", e);
      sendErrorResponse(context, 500, "Internal server error");
    }
  }

  /** Handles the multi-city weather endpoint request. */
  public void handleMultiCityWeather(RoutingContext context) {
    logger.info("Handler received multi-city weather request: {}", context.request().path());

    try {
      List<String> cities = parseCitiesFromRequest(context);

      if (cities == null) {
        return;
      }

      weatherService
          .getMultiCityWeather(cities)
          .onComplete(
              ar -> {
                if (ar.succeeded()) {
                  logger.info("Successfully processed multi-city weather request for {} cities", 
                      cities.size());
                  sendSuccessResponse(context, ar.result());
                } else {
                  logger.error("Error while processing multi-city weather request", ar.cause());
                  sendErrorResponse(
                      context, 500, "Internal server error: " + ar.cause().getMessage());
                }
              });

    } catch (Exception e) {
      logger.error("Unexpected error in multi-city weather handler", e);
      sendErrorResponse(context, 400, "Invalid request format");
    }
  }

  /** Sends a successful JSON response. */
  private void sendSuccessResponse(RoutingContext context, Object data) {
    HttpServerResponse response = context.response();
    response.putHeader("content-type", "application/json");
    
    if (data instanceof MultiCityWeatherResponse) {
      MultiCityWeatherResponse weatherResponse = (MultiCityWeatherResponse) data;
      JsonObject jsonResponse = buildMultiCityWeatherJson(weatherResponse);
      response.end(jsonResponse.encodePrettily());
    } else {
      response.end(Json.encodePrettily(data));
    }
  }

  /** Parses and validates cities from the request body. */
  private List<String> parseCitiesFromRequest(RoutingContext context) {
    JsonObject jsonBody = context.body().asJsonObject();
    
    // Validate cities array exists
    if (!jsonBody.containsKey("cities") || jsonBody.getValue("cities") == null) {
      sendErrorResponse(context, 400, "Request body must contain a 'cities' array");
      return null;
    }
    
    JsonArray citiesArray = jsonBody.getJsonArray("cities");
    if (citiesArray.isEmpty()) {
      sendErrorResponse(context, 400, "Request body must contain a non-empty 'cities' array");
      return null;
    }
    
    // Convert to List<String> with validation
    List<String> cities = new ArrayList<>();
    for (int i = 0; i < citiesArray.size(); i++) {
      String city = citiesArray.getString(i);
      if (city == null || city.trim().isEmpty()) {
        sendErrorResponse(context, 400, "City names cannot be null or empty");
        return null;
      }
      cities.add(city);
    }
    
    return cities;
  }

  /** Builds JSON response for multi-city weather data. */
  private JsonObject buildMultiCityWeatherJson(MultiCityWeatherResponse response) {
    JsonObject jsonResponse = new JsonObject();
    jsonResponse.put("totalCities", response.getTotalCities());
    jsonResponse.put("successfulRequests", response.getSuccessfulRequests());
    jsonResponse.put("failedRequests", response.getFailedRequests());
    
    JsonArray weatherDataArray = new JsonArray();
    for (WeatherData weatherData : response.getWeatherData()) {
      JsonObject cityWeather = new JsonObject();
      cityWeather.put("city", weatherData.getCity());
      cityWeather.put("temperature", weatherData.getTemperature());
      cityWeather.put("description", weatherData.getDescription());
      cityWeather.put("humidity", weatherData.getHumidity());
      cityWeather.put("pressure", weatherData.getPressure());
      cityWeather.put("windSpeed", weatherData.getWindSpeed());
      
      JsonObject coordinates = new JsonObject();
      coordinates.put("latitude", weatherData.getCoordinates().getLatitude());
      coordinates.put("longitude", weatherData.getCoordinates().getLongitude());
      cityWeather.put("coordinates", coordinates);
      
      weatherDataArray.add(cityWeather);
    }
    
    jsonResponse.put("weatherData", weatherDataArray);
    return jsonResponse;
  }

  /** Sends an error response. */
  private void sendErrorResponse(RoutingContext context, int statusCode, String message) {
    HttpServerResponse response = context.response();
    response.setStatusCode(statusCode);
    response.putHeader("content-type", "application/json");

    ApiError error = new ApiError("API_ERROR", message);
    response.end(Json.encodePrettily(error));
  }

  /** Extracts a double query parameter from the request. */
  private double getQueryParameterDouble(String param, RoutingContext context) {
    String paramValue = context.request().getParam(param);
    if (paramValue != null && !paramValue.isEmpty()) {
      try {
        return Double.parseDouble(paramValue);
      } catch (NumberFormatException e) {
        logger.warn("Invalid number format for parameter '{}': {}", param, paramValue);
        return -1.0;
      }
    }
    return -1.0;
  }

  /**
   * Validates the air pollution request parameters.
   *
   * @param latitude the latitude coordinate
   * @param longitude the longitude coordinate
   * @return validation error message if invalid, null if valid
   */
  private String validateAirPollutionRequest(double latitude, double longitude) {
    if (latitude == -1.0) {
      return "Missing required parameter: latitude";
    }

    if (longitude == -1.0) {
      return "Missing required parameter: longitude";
    }

    if (latitude < -90.0 || latitude > 90.0) {
      return "Latitude must be between -90 and 90 degrees";
    }

    if (longitude < -180.0 || longitude > 180.0) {
      return "Longitude must be between -180 and 180 degrees";
    }

    return null; // No validation errors
  }
}
