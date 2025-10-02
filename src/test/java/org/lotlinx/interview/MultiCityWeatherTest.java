package org.lotlinx.interview;

import static org.junit.jupiter.api.Assertions.*;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lotlinx.interview.config.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(VertxExtension.class)
public class MultiCityWeatherTest {

  private static final Logger logger = LoggerFactory.getLogger(MultiCityWeatherTest.class);
  private static final String BASE_URL = "http://localhost:8080";
  private static final String WEATHER_ENDPOINT = BASE_URL + "/api/v1/weather/multi-city";

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    logger.info("Deploying MainServerVerticle for multi-city weather testing");
    vertx.deployVerticle(
        new MainServerVerticle(),
        testContext.succeeding(
            id -> {
              logger.info("MainServerVerticle deployed successfully with ID: {}", id);
              testContext.completeNow();
            }));
  }

  @Test
  void test_valid_cities_weather(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    JsonObject requestBody = new JsonObject()
        .put("cities", new JsonArray().add("Toronto").add("Winnipeg"));

    logger.info("Testing valid cities weather request: {}", requestBody.encode());

    client
        .postAbs(WEATHER_ENDPOINT)
        .putHeader("Content-Type", "application/json")
        .sendBuffer(Buffer.buffer(requestBody.encode()))
        .onSuccess(
            response -> {
              testContext.verify(
                  () -> {
                    assertEquals(200, response.statusCode());
                    
                    JsonObject responseBody = response.bodyAsJsonObject();
                    assertNotNull(responseBody);
                    
                    // Verify response structure
                    assertTrue(responseBody.containsKey("totalCities"));
                    assertTrue(responseBody.containsKey("successfulRequests"));
                    assertTrue(responseBody.containsKey("failedRequests"));
                    assertTrue(responseBody.containsKey("weatherData"));
                    
                    // Verify counts
                    assertEquals(2, responseBody.getInteger("totalCities"));
                    assertEquals(2, responseBody.getInteger("successfulRequests"));
                    assertEquals(0, responseBody.getInteger("failedRequests"));
                    
                    // Verify weather data
                    JsonArray weatherData = responseBody.getJsonArray("weatherData");
                    assertNotNull(weatherData);
                    assertEquals(2, weatherData.size());
                    
                    // Verify each city has required fields
                    for (int i = 0; i < weatherData.size(); i++) {
                      JsonObject cityWeather = weatherData.getJsonObject(i);
                      assertNotNull(cityWeather);
                      
                      assertTrue(cityWeather.containsKey("city"));
                      assertTrue(cityWeather.containsKey("temperature"));
                      assertTrue(cityWeather.containsKey("description"));
                      assertTrue(cityWeather.containsKey("humidity"));
                      assertTrue(cityWeather.containsKey("pressure"));
                      assertTrue(cityWeather.containsKey("windSpeed"));
                      assertTrue(cityWeather.containsKey("coordinates"));
                      
                      // Verify coordinates structure
                      JsonObject coordinates = cityWeather.getJsonObject("coordinates");
                      assertNotNull(coordinates);
                      assertTrue(coordinates.containsKey("latitude"));
                      assertTrue(coordinates.containsKey("longitude"));
                      
                      // Verify data types
                      assertNotNull(cityWeather.getString("city"));
                      assertNotNull(cityWeather.getDouble("temperature"));
                      assertNotNull(cityWeather.getString("description"));
                      assertNotNull(cityWeather.getInteger("humidity"));
                      assertNotNull(cityWeather.getDouble("pressure"));
                      assertNotNull(cityWeather.getDouble("windSpeed"));
                    }
                    
                    logger.info("Valid cities weather test passed");
                  });
              testContext.completeNow();
            })
        .onFailure(
            throwable -> {
              logger.error("Valid cities weather test failed", throwable);
              testContext.failNow(throwable);
            });
  }

  @Test
  void test_empty_cities_list(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    JsonObject requestBody = new JsonObject()
        .put("cities", new JsonArray());

    logger.info("Testing empty cities list request: {}", requestBody.encode());

    client
        .postAbs(WEATHER_ENDPOINT)
        .putHeader("Content-Type", "application/json")
        .sendBuffer(Buffer.buffer(requestBody.encode()))
        .onSuccess(
            response -> {
              testContext.verify(
                  () -> {
                    assertEquals(200, response.statusCode());
                    
                    JsonObject responseBody = response.bodyAsJsonObject();
                    assertNotNull(responseBody);
                    
                    // Verify response for empty list
                    assertEquals(0, responseBody.getInteger("totalCities"));
                    assertEquals(0, responseBody.getInteger("successfulRequests"));
                    assertEquals(0, responseBody.getInteger("failedRequests"));
                    
                    JsonArray weatherData = responseBody.getJsonArray("weatherData");
                    assertNotNull(weatherData);
                    assertEquals(0, weatherData.size());
                    
                    logger.info("Empty cities list test passed");
                  });
              testContext.completeNow();
            })
        .onFailure(
            throwable -> {
              logger.error("Empty cities list test failed", throwable);
              testContext.failNow(throwable);
            });
  }

  @Test
  void test_invalid_city_name(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    JsonObject requestBody = new JsonObject()
        .put("cities", new JsonArray().add("InvalidCityName12345").add("Toronto"));

    logger.info("Testing invalid city name request: {}", requestBody.encode());

    client
        .postAbs(WEATHER_ENDPOINT)
        .putHeader("Content-Type", "application/json")
        .sendBuffer(Buffer.buffer(requestBody.encode()))
        .onSuccess(
            response -> {
              testContext.verify(
                  () -> {
                    assertEquals(200, response.statusCode());
                    
                    JsonObject responseBody = response.bodyAsJsonObject();
                    assertNotNull(responseBody);
                    
                    // Verify response structure
                    assertEquals(2, responseBody.getInteger("totalCities"));
                    assertEquals(1, responseBody.getInteger("successfulRequests"));
                    assertEquals(1, responseBody.getInteger("failedRequests"));
                    
                    // Verify weather data (should have 1 successful city)
                    JsonArray weatherData = responseBody.getJsonArray("weatherData");
                    assertNotNull(weatherData);
                    assertEquals(1, weatherData.size());
                    
                    // Verify failed cities are reported
                    if (responseBody.containsKey("failedCities")) {
                      JsonArray failedCities = responseBody.getJsonArray("failedCities");
                      assertNotNull(failedCities);
                      assertEquals(1, failedCities.size());
                      assertEquals("InvalidCityName12345", failedCities.getString(0));
                    }
                    
                    // Verify the successful city data
                    JsonObject cityWeather = weatherData.getJsonObject(0);
                    assertNotNull(cityWeather);
                    assertEquals("Toronto", cityWeather.getString("city"));
                    
                    logger.info("Invalid city name test passed");
                  });
              testContext.completeNow();
            })
        .onFailure(
            throwable -> {
              logger.error("Invalid city name test failed", throwable);
              testContext.failNow(throwable);
            });
  }

  @Test
  void test_malformed_json_request(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    String malformedJson = "{\"cities\": [\"Toronto\", }"; // Invalid JSON

    logger.info("Testing malformed JSON request");

    client
        .postAbs(WEATHER_ENDPOINT)
        .putHeader("Content-Type", "application/json")
        .sendBuffer(Buffer.buffer(malformedJson))
        .onSuccess(
            response -> {
              testContext.verify(
                  () -> {
                    assertEquals(400, response.statusCode());
                    logger.info("Malformed JSON test passed - correctly returned 400");
                  });
              testContext.completeNow();
            })
        .onFailure(
            throwable -> {
              logger.error("Malformed JSON test failed", throwable);
              testContext.failNow(throwable);
            });
  }

  @Test
  void test_missing_cities_field(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    JsonObject requestBody = new JsonObject()
        .put("wrongField", new JsonArray().add("Toronto"));

    logger.info("Testing missing cities field request: {}", requestBody.encode());

    client
        .postAbs(WEATHER_ENDPOINT)
        .putHeader("Content-Type", "application/json")
        .sendBuffer(Buffer.buffer(requestBody.encode()))
        .onSuccess(
            response -> {
              testContext.verify(
                  () -> {
                    // Should handle gracefully - either 400 or empty response
                    assertTrue(response.statusCode() == 400 || response.statusCode() == 200);
                    
                    if (response.statusCode() == 200) {
                      JsonObject responseBody = response.bodyAsJsonObject();
                      assertNotNull(responseBody);
                      assertEquals(0, responseBody.getInteger("totalCities"));
                    }
                    
                    logger.info("Missing cities field test passed");
                  });
              testContext.completeNow();
            })
        .onFailure(
            throwable -> {
              logger.error("Missing cities field test failed", throwable);
              testContext.failNow(throwable);
            });
  }

  @Test
  void test_large_cities_list(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    JsonArray cities = new JsonArray()
        .add("Toronto")
        .add("Vancouver")
        .add("Montreal")
        .add("Calgary")
        .add("Ottawa");
    
    JsonObject requestBody = new JsonObject().put("cities", cities);

    logger.info("Testing large cities list request with {} cities", cities.size());

    client
        .postAbs(WEATHER_ENDPOINT)
        .putHeader("Content-Type", "application/json")
        .sendBuffer(Buffer.buffer(requestBody.encode()))
        .onSuccess(
            response -> {
              testContext.verify(
                  () -> {
                    assertEquals(200, response.statusCode());
                    
                    JsonObject responseBody = response.bodyAsJsonObject();
                    assertNotNull(responseBody);
                    
                    assertEquals(5, responseBody.getInteger("totalCities"));
                    
                    // Should have some successful requests (at least 3 major Canadian cities)
                    int successful = responseBody.getInteger("successfulRequests");
                    assertTrue(successful >= 3, "Expected at least 3 successful requests, got " + successful);
                    
                    JsonArray weatherData = responseBody.getJsonArray("weatherData");
                    assertNotNull(weatherData);
                    assertEquals(successful, weatherData.size());
                    
                    logger.info("Large cities list test passed with {} successful requests", successful);
                  });
              testContext.completeNow();
            })
        .onFailure(
            throwable -> {
              logger.error("Large cities list test failed", throwable);
              testContext.failNow(throwable);
            });
  }
}
