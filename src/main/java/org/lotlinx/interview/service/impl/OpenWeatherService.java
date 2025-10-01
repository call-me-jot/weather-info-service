package org.lotlinx.interview.service.impl;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.lotlinx.interview.config.OpenWeatherConfig;
import org.lotlinx.interview.model.*;
import org.lotlinx.interview.service.WeatherService;
import org.lotlinx.interview.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

  @Override
  public Future<MultiCityWeatherResponse> getMultiCityWeather(List<String> cities) {
    Promise<MultiCityWeatherResponse> promise = Promise.promise();

    if (cities == null || cities.isEmpty()) {
      promise.complete(new MultiCityWeatherResponse(new ArrayList<>(), 0, 0, 0));
      return promise.future();
    }

    logger.info("Fetching weather data for {} cities: {}", cities.size(), cities);

    List<WeatherData> weatherDataList = new ArrayList<>();
    AtomicInteger completedRequests = new AtomicInteger(0);
    AtomicInteger failedRequests = new AtomicInteger(0);
    int totalCities = cities.size();

    // Process each city asynchronously
    for (String city : cities) {
      getWeatherForCity(city)
          .onComplete(ar -> {
            synchronized (weatherDataList) {
              if (ar.succeeded()) {
                weatherDataList.add(ar.result());
                logger.debug("Successfully retrieved weather for city: {}", city);
              } else {
                logger.warn("Failed to retrieve weather for city: {} - {}", city, ar.cause().getMessage());
                failedRequests.incrementAndGet();
              }

              int completed = completedRequests.incrementAndGet();
              if (completed == totalCities) {
                // All requests completed
                MultiCityWeatherResponse response = new MultiCityWeatherResponse(
                    weatherDataList,
                    totalCities,
                    weatherDataList.size(),
                    failedRequests.get()
                );
                promise.complete(response);
              }
            }
          });
    }

    return promise.future();
  }

  /**
   * Retrieves weather data for a single city by first getting coordinates via geocoding,
   * then fetching current weather data.
   */
  private Future<WeatherData> getWeatherForCity(String cityName) {
    Promise<WeatherData> promise = Promise.promise();

    // First, get coordinates for the city
    getCityCoordinates(cityName)
        .compose(geocodingResponse -> {
          if (geocodingResponse == null || geocodingResponse.length == 0) {
            return Future.failedFuture("City not found: " + cityName);
          }

          GeocodingResponse cityData = geocodingResponse[0];
          Coordinates coordinates = new Coordinates(cityData.getLatitude(), cityData.getLongitude());

          // Then get current weather for the coordinates
          return getCurrentWeather(coordinates)
              .map(currentWeather -> mapToWeatherData(cityName, currentWeather, coordinates));
        })
        .onComplete(ar -> {
          if (ar.succeeded()) {
            promise.complete(ar.result());
          } else {
            promise.fail(ar.cause());
          }
        });

    return promise.future();
  }

  /**
   * Gets coordinates for a city using the Geocoding API.
   */
  private Future<GeocodingResponse[]> getCityCoordinates(String cityName) {
    Promise<GeocodingResponse[]> promise = Promise.promise();

    MultiMap queryParams = buildGeocodingQueryParams(cityName);
    OpenWeatherConfig config = OpenWeatherConfig.forGeocoding();

    httpClient
        .sendGetRequestRaw(config.getHost(), config.getPath(), queryParams, config.getPort())
        .onComplete(ar -> {
          if (ar.succeeded()) {
            try {
              GeocodingResponse[] responses = parseGeocodingResponse(ar.result());
              promise.complete(responses);
            } catch (Exception e) {
              logger.error("Failed to parse geocoding response for city: {}", cityName, e);
              promise.fail(new RuntimeException("Failed to parse geocoding data", e));
            }
          } else {
            logger.error("Failed to fetch coordinates for city: {}", cityName, ar.cause());
            promise.fail(ar.cause());
          }
        });

    return promise.future();
  }

  /**
   * Gets current weather data for given coordinates.
   */
  private Future<CurrentWeatherResponse> getCurrentWeather(Coordinates coordinates) {
    Promise<CurrentWeatherResponse> promise = Promise.promise();

    MultiMap queryParams = buildWeatherQueryParams(coordinates);
    OpenWeatherConfig config = OpenWeatherConfig.forCurrentWeather();

    httpClient
        .sendGetRequest(config.getHost(), config.getPath(), queryParams, config.getPort())
        .onComplete(ar -> {
          if (ar.succeeded()) {
            try {
              CurrentWeatherResponse response = parseWeatherResponse(ar.result());
              promise.complete(response);
            } catch (Exception e) {
              logger.error("Failed to parse current weather response", e);
              promise.fail(new RuntimeException("Failed to parse weather data", e));
            }
          } else {
            logger.error("Failed to fetch current weather data", ar.cause());
            promise.fail(ar.cause());
          }
        });

    return promise.future();
  }

  /**
   * Builds query parameters for geocoding API.
   */
  private MultiMap buildGeocodingQueryParams(String cityName) {
    MultiMap queryParams = MultiMap.caseInsensitiveMultiMap();
    queryParams.add("q", cityName);
    queryParams.add("limit", "1");
    queryParams.add("appid", OpenWeatherConfig.forGeocoding().getApiKey());
    return queryParams;
  }

  /**
   * Builds query parameters for weather API.
   */
  private MultiMap buildWeatherQueryParams(Coordinates coordinates) {
    MultiMap queryParams = MultiMap.caseInsensitiveMultiMap();
    queryParams.add("lat", String.valueOf(coordinates.getLatitude()));
    queryParams.add("lon", String.valueOf(coordinates.getLongitude()));
    queryParams.add("appid", OpenWeatherConfig.forCurrentWeather().getApiKey());
    queryParams.add("units", "metric");
    return queryParams;
  }

  /**
   * Parses geocoding API response.
   */
  private GeocodingResponse[] parseGeocodingResponse(String responseBody) {
    JsonArray responseArray = new JsonArray(responseBody);
    GeocodingResponse[] geocodingResponses = new GeocodingResponse[responseArray.size()];
    
    for (int i = 0; i < responseArray.size(); i++) {
      JsonObject cityJson = responseArray.getJsonObject(i);
      GeocodingResponse geocodingResponse = new GeocodingResponse();
      geocodingResponse.setName(cityJson.getString("name"));
      geocodingResponse.setLatitude(cityJson.getDouble("lat"));
      geocodingResponse.setLongitude(cityJson.getDouble("lon"));
      geocodingResponse.setCountry(cityJson.getString("country"));
      geocodingResponse.setState(cityJson.getString("state"));
      geocodingResponses[i] = geocodingResponse;
    }
    
    return geocodingResponses;
  }

  /**
   * Parses weather API response.
   */
  private CurrentWeatherResponse parseWeatherResponse(JsonObject response) {
    CurrentWeatherResponse weatherResponse = new CurrentWeatherResponse();
    weatherResponse.setCityName(response.getString("name"));
    
    // Parse main weather data
    JsonObject main = response.getJsonObject("main");
    if (main != null) {
      CurrentWeatherResponse.MainWeatherData mainData = new CurrentWeatherResponse.MainWeatherData();
      mainData.setTemperature(main.getDouble("temp"));
      mainData.setHumidity(main.getInteger("humidity"));
      mainData.setPressure(main.getDouble("pressure"));
      weatherResponse.setMain(mainData);
    }
    
    // Parse weather description
    JsonArray weatherArray = response.getJsonArray("weather");
    if (weatherArray != null && weatherArray.size() > 0) {
      JsonObject weatherObj = weatherArray.getJsonObject(0);
      CurrentWeatherResponse.Weather weather = new CurrentWeatherResponse.Weather();
      weather.setDescription(weatherObj.getString("description"));
      weatherResponse.setWeather(List.of(weather));
    }
    
    // Parse wind data
    JsonObject wind = response.getJsonObject("wind");
    if (wind != null) {
      CurrentWeatherResponse.Wind windData = new CurrentWeatherResponse.Wind();
      windData.setSpeed(wind.getDouble("speed"));
      weatherResponse.setWind(windData);
    }
    
    // Parse coordinates
    JsonObject coord = response.getJsonObject("coord");
    if (coord != null) {
      Coordinates responseCoordinates = new Coordinates();
      responseCoordinates.setLatitude(coord.getDouble("lat"));
      responseCoordinates.setLongitude(coord.getDouble("lon"));
      weatherResponse.setCoord(responseCoordinates);
    }
    
    return weatherResponse;
  }

  /**
   * Maps CurrentWeatherResponse to WeatherData.
   */
  private WeatherData mapToWeatherData(String cityName, CurrentWeatherResponse weatherResponse, Coordinates coordinates) {
    String description = weatherResponse.getWeather() != null && !weatherResponse.getWeather().isEmpty()
        ? weatherResponse.getWeather().get(0).getDescription()
        : "No description available";

    double temperature = weatherResponse.getMain() != null ? weatherResponse.getMain().getTemperature() : 0.0;
    int humidity = weatherResponse.getMain() != null ? weatherResponse.getMain().getHumidity() : 0;
    double pressure = weatherResponse.getMain() != null ? weatherResponse.getMain().getPressure() : 0.0;
    double windSpeed = weatherResponse.getWind() != null ? weatherResponse.getWind().getSpeed() : 0.0;

    return new WeatherData(
        cityName,
        temperature,
        description,
        humidity,
        pressure,
        windSpeed,
        coordinates
    );
  }
}
