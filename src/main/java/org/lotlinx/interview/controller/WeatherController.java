package org.lotlinx.interview.controller;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.ArrayList;
import java.util.List;
import org.lotlinx.interview.model.AirPollutionResponse;
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

    } catch (IllegalArgumentException e) {
      logger.warn("Invalid parameters for air pollution request: {}", e.getMessage());
      sendErrorResponse(context, 400, e.getMessage());
    } catch (Exception e) {
      logger.error("Unexpected error in air pollution handler", e);
      sendErrorResponse(context, 500, e.getMessage());
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
                  sendErrorResponse(context, 500, ar.cause().getMessage());
                }
              });

    } catch (Exception e) {
      logger.error("Unexpected error in multi-city weather handler", e);
      sendErrorResponse(context, 400, e.getMessage());
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
    } else if (data instanceof AirPollutionResponse) {
      AirPollutionResponse airPollutionResponse = (AirPollutionResponse) data;
      JsonObject jsonResponse = buildAirPollutionJson(airPollutionResponse);
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
    
    // Add failed cities information
    if (response.getFailedCities() != null && !response.getFailedCities().isEmpty()) {
      JsonArray failedCitiesArray = new JsonArray();
      for (String failedCity : response.getFailedCities()) {
        failedCitiesArray.add(failedCity);
      }
      jsonResponse.put("failedCities", failedCitiesArray);
    }
    
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

  /** Builds JSON response for air pollution data. */
  private JsonObject buildAirPollutionJson(AirPollutionResponse response) {
    JsonObject jsonResponse = new JsonObject();
    
    // Add coordinates
    if (response.getCoord() != null) {
      JsonObject coord = new JsonObject();
      coord.put("longitude", response.getCoord().getLongitude());
      coord.put("latitude", response.getCoord().getLatitude());
      jsonResponse.put("coord", coord);
    }
    
    // Add air pollution data list
    if (response.getList() != null && !response.getList().isEmpty()) {
      JsonArray listArray = new JsonArray();
      
      for (AirPollutionResponse.AirPollutionData data : response.getList()) {
        JsonObject dataJson = new JsonObject();
        
        // Add main data (AQI)
        if (data.getMain() != null) {
          JsonObject main = new JsonObject();
          main.put("aqi", data.getMain().getAqi());
          dataJson.put("main", main);
        }
        
        // Add components
        if (data.getComponents() != null) {
          JsonObject components = new JsonObject();
          components.put("co", data.getComponents().getCo());
          components.put("no", data.getComponents().getNo());
          components.put("no2", data.getComponents().getNo2());
          components.put("o3", data.getComponents().getO3());
          components.put("so2", data.getComponents().getSo2());
          components.put("pm2_5", data.getComponents().getPm25());
          components.put("pm10", data.getComponents().getPm10());
          components.put("nh3", data.getComponents().getNh3());
          dataJson.put("components", components);
        }
        
        // Add timestamp
        dataJson.put("dt", data.getTimestamp());
        
        listArray.add(dataJson);
      }
      
      jsonResponse.put("list", listArray);
    }
    
    return jsonResponse;
  }

  /** Sends an error response. */
  private void sendErrorResponse(RoutingContext context, int statusCode, String message) {
    HttpServerResponse response = context.response();
    response.setStatusCode(statusCode);
    response.putHeader("content-type", "application/json");

    JsonObject errorJson = new JsonObject();
    errorJson.put("error", "API_ERROR");
    errorJson.put("message", message);
    
    response.end(errorJson.encodePrettily());
  }

  /** Extracts a double query parameter from the request. */
  private double getQueryParameterDouble(String param, RoutingContext context) 
      throws IllegalArgumentException {
    String paramValue = context.request().getParam(param);
    
    if (paramValue == null || paramValue.isEmpty()) {
      throw new IllegalArgumentException("Missing required parameter: " + param);
    }
    
    try {
      return Double.parseDouble(paramValue);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid number format for parameter '" + param + "': " + paramValue);
    }
  }

}
