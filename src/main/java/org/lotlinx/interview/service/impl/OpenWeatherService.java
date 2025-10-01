package org.lotlinx.interview.service.impl;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.lotlinx.interview.config.OpenWeatherConfig;
import org.lotlinx.interview.model.AirPollutionResponse;
import org.lotlinx.interview.service.WeatherService;
import org.lotlinx.interview.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implementation of WeatherService that integrates with OpenWeatherMap API. */
public class OpenWeatherService implements WeatherService {

  private static final Logger logger = LoggerFactory.getLogger(OpenWeatherService.class);

  private final Vertx vertx;
  private final OpenWeatherConfig config;
  private final HttpClientUtil httpClient;

  public OpenWeatherService(Vertx vertx) {
    this.vertx = vertx;
    this.config = new OpenWeatherConfig();
    this.httpClient = new HttpClientUtil(vertx);
  }

  public OpenWeatherService(Vertx vertx, OpenWeatherConfig config) {
    this.vertx = vertx;
    this.config = config;
    this.httpClient = new HttpClientUtil(vertx);
  }

  @Override
  public Future<AirPollutionResponse> getCurrentAirPollution(double latitude, double longitude) {
    Promise<AirPollutionResponse> promise = Promise.promise();

    logger.debug(
        "Fetching air pollution data for coordinates: lat={}, lon={}", latitude, longitude);

    // Build query parameters
    MultiMap queryParams = MultiMap.caseInsensitiveMultiMap();
    queryParams.add("lat", String.valueOf(latitude));
    queryParams.add("lon", String.valueOf(longitude));
    queryParams.add("appId", config.getApiKey());

    // Make HTTP request
    httpClient
        .sendGetRequest(config.getHost(), config.getPath(), queryParams, config.getPort())
        .onComplete(
            ar -> {
              if (ar.succeeded()) {
                try {
                  JsonObject response = ar.result();
                  AirPollutionResponse airPollutionResponse =
                      response.mapTo(AirPollutionResponse.class);
                  logger.info("Successfully retrieved air pollution data");
                  promise.complete(airPollutionResponse);
                } catch (Exception e) {
                  logger.error("Failed to parse air pollution response", e);
                  promise.fail(new RuntimeException("Failed to parse air pollution data", e));
                }
              } else {
                logger.error("Failed to fetch air pollution data", ar.cause());
                promise.fail(ar.cause());
              }
            });

    return promise.future();
  }
}
