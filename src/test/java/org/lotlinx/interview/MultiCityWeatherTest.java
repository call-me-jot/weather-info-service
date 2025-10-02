package org.lotlinx.interview;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

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
  void test_valid_cities_multi_weather(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    JsonArray cities = new JsonArray().add("Toronto").add("Delhi");
    JsonObject requestBody = new JsonObject().put("cities", cities);

    logger.info("Testing multi-city weather endpoint with valid cities: {}", WEATHER_ENDPOINT);

    client
        .postAbs(WEATHER_ENDPOINT)
        .putHeader("Content-Type", "application/json")
        .sendJsonObject(
            requestBody,
            testContext.succeeding(
                response -> {
                  testContext.verify(
                      () -> {
                        assertEquals(200, response.statusCode());
                        JsonObject responseBody = response.bodyAsJsonObject();
                        
                        assertNotNull(responseBody);
                        assertTrue(responseBody.containsKey("totalCities"));
                        assertTrue(responseBody.containsKey("successfulRequests"));
                        assertTrue(responseBody.containsKey("failedRequests"));
                        assertTrue(responseBody.containsKey("weatherData"));
                        
                        assertEquals(2, responseBody.getInteger("totalCities"));
                        assertTrue(responseBody.getInteger("successfulRequests") >= 0);
                        assertTrue(responseBody.getInteger("failedRequests") >= 0);
                        
                        logger.info("Multi-city weather test passed: {}", responseBody.encodePrettily());
                      });
                  testContext.completeNow();
                }));
  }

  @Test
  void test_empty_cities_list(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    JsonArray cities = new JsonArray();
    JsonObject requestBody = new JsonObject().put("cities", cities);

    logger.info("Testing multi-city weather endpoint with empty cities list");

    client
        .postAbs(WEATHER_ENDPOINT)
        .putHeader("Content-Type", "application/json")
        .sendJsonObject(
            requestBody,
            testContext.succeeding(
                response -> {
                  testContext.verify(
                      () -> {
                        assertEquals(400, response.statusCode());
                        JsonObject responseBody = response.bodyAsJsonObject();
                        
                        assertNotNull(responseBody);
                        assertTrue(responseBody.containsKey("error"));
                        assertTrue(responseBody.containsKey("message"));
                        assertTrue(responseBody.getString("message").contains("non-empty 'cities' array"));
                        
                        logger.info("Empty cities list test passed");
                      });
                  testContext.completeNow();
                }));
  }

  @Test
  void test_invalid_city_names(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    JsonArray cities = new JsonArray().add("InvalidCityXYZ123").add("AnotherInvalidCity999");
    JsonObject requestBody = new JsonObject().put("cities", cities);

    logger.info("Testing multi-city weather endpoint with invalid city names");

    client
        .postAbs(WEATHER_ENDPOINT)
        .putHeader("Content-Type", "application/json")
        .sendJsonObject(
            requestBody,
            testContext.succeeding(
                response -> {
                  testContext.verify(
                      () -> {
                        assertEquals(200, response.statusCode());
                        JsonObject responseBody = response.bodyAsJsonObject();
                        
                        assertNotNull(responseBody);
                        assertEquals(2, responseBody.getInteger("totalCities"));
                        assertTrue(responseBody.containsKey("failedCities"));
                        
                        // Should have failed cities since these are invalid
                        assertTrue(responseBody.getInteger("failedRequests") > 0);
                        
                        logger.info("Invalid city names test passed: failed requests = {}", 
                                  responseBody.getInteger("failedRequests"));
                      });
                  testContext.completeNow();
                }));
  }

  @Test
  void test_malformed_json_request(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    String malformedJson = "{\"cities\": [\"Toronto\", \"Delhi\""; // Missing closing bracket and brace

    logger.info("Testing multi-city weather endpoint with malformed JSON");

    client
        .postAbs(WEATHER_ENDPOINT)
        .putHeader("Content-Type", "application/json")
        .sendBuffer(
            io.vertx.core.buffer.Buffer.buffer(malformedJson),
            testContext.succeeding(
                response -> {
                  testContext.verify(
                      () -> {
                        assertEquals(400, response.statusCode());
                        logger.info("Malformed JSON test passed: status = {}", response.statusCode());
                      });
                  testContext.completeNow();
                }));
  }

  @Test
  void test_missing_cities_field(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    JsonArray cities = new JsonArray().add("Toronto");
    JsonObject requestBody = new JsonObject().put("wrongField", cities);

    logger.info("Testing multi-city weather endpoint with missing cities field");

    client
        .postAbs(WEATHER_ENDPOINT)
        .putHeader("Content-Type", "application/json")
        .sendJsonObject(
            requestBody,
            testContext.succeeding(
                response -> {
                  testContext.verify(
                      () -> {
                        assertEquals(400, response.statusCode());
                        logger.info("Missing cities field test passed: status = {}", response.statusCode());
                      });
                  testContext.completeNow();
                }));
  }

  @Test
  void test_mixed_valid_invalid_cities(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    JsonArray cities = new JsonArray().add("Toronto").add("InvalidCityXYZ").add("Delhi");
    JsonObject requestBody = new JsonObject().put("cities", cities);

    logger.info("Testing multi-city weather endpoint with mixed valid/invalid cities");

    client
        .postAbs(WEATHER_ENDPOINT)
        .putHeader("Content-Type", "application/json")
        .sendJsonObject(
            requestBody,
            testContext.succeeding(
                response -> {
                  testContext.verify(
                      () -> {
                        assertEquals(200, response.statusCode());
                        JsonObject responseBody = response.bodyAsJsonObject();
                        
                        assertNotNull(responseBody);
                        assertEquals(3, responseBody.getInteger("totalCities"));
                        
                        // Should have both successful and failed requests
                        int successful = responseBody.getInteger("successfulRequests");
                        int failed = responseBody.getInteger("failedRequests");
                        
                        assertTrue(successful > 0, "Should have at least one successful request");
                        assertTrue(failed > 0, "Should have at least one failed request");
                        assertEquals(3, successful + failed, "Total should equal successful + failed");
                        
                        logger.info("Mixed cities test passed: successful = {}, failed = {}", successful, failed);
                      });
                  testContext.completeNow();
                }));
  }

  @Test
  void test_large_cities_list(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    JsonArray cities = new JsonArray()
        .add("Toronto").add("Delhi").add("Mumbai").add("London").add("Paris")
        .add("Tokyo").add("Sydney").add("New York").add("Los Angeles").add("Chicago");
    JsonObject requestBody = new JsonObject().put("cities", cities);

    logger.info("Testing multi-city weather endpoint with large cities list (10 cities)");

    client
        .postAbs(WEATHER_ENDPOINT)
        .putHeader("Content-Type", "application/json")
        .sendJsonObject(
            requestBody,
            testContext.succeeding(
                response -> {
                  testContext.verify(
                      () -> {
                        assertEquals(200, response.statusCode());
                        JsonObject responseBody = response.bodyAsJsonObject();
                        
                        assertNotNull(responseBody);
                        assertEquals(10, responseBody.getInteger("totalCities"));
                        
                        int successful = responseBody.getInteger("successfulRequests");
                        int failed = responseBody.getInteger("failedRequests");
                        assertEquals(10, successful + failed);
                        
                        logger.info("Large cities list test passed: processed {} cities", 
                                  responseBody.getInteger("totalCities"));
                      });
                  testContext.completeNow();
                }));
  }
}
