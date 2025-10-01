package org.lotlinx.interview.controller;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.lotlinx.interview.model.ApiError;
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
      if (latitude == -1.0 || longitude == -1.0) {
        sendErrorResponse(context, 400, "Missing required parameters: latitude and longitude");
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

  /** Sends a successful JSON response. */
  private void sendSuccessResponse(RoutingContext context, Object data) {
    HttpServerResponse response = context.response();
    response.putHeader("content-type", "application/json");
    response.end(Json.encodePrettily(data));
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
}
