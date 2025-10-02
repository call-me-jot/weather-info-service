package org.lotlinx.interview;

import static org.junit.jupiter.api.Assertions.*;

import io.vertx.core.Vertx;
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
public class AirPollutionTest {

  private static final Logger logger = LoggerFactory.getLogger(AirPollutionTest.class);
  private static final String BASE_URL = "http://localhost:8080";
  private static final String AIR_POLLUTION_ENDPOINT = BASE_URL + "/getCurrentAirPollution";

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    logger.info("Deploying MainServerVerticle for air pollution testing");
    vertx.deployVerticle(
        new MainServerVerticle(),
        testContext.succeeding(
            id -> {
              logger.info("MainServerVerticle deployed successfully with ID: {}", id);
              testContext.completeNow();
            }));
  }

  @Test
  void test_valid_coordinates_air_pollution(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    String testUrl = AIR_POLLUTION_ENDPOINT + "?latitude=43.6534817&longitude=-79.3839347";

    logger.info("Testing air pollution endpoint with valid coordinates: {}", testUrl);

    client
        .getAbs(testUrl)
        .send()
        .onSuccess(
            response -> {
              testContext.verify(
                  () -> {
                    assertEquals(200, response.statusCode());
                    
                    JsonObject responseBody = response.bodyAsJsonObject();
                    assertNotNull(responseBody);
                    
                    // Verify response structure
                    assertTrue(responseBody.containsKey("coord"));
                    assertTrue(responseBody.containsKey("list"));
                    
                    // Verify coordinates
                    JsonObject coord = responseBody.getJsonObject("coord");
                    assertNotNull(coord);
                    assertTrue(coord.containsKey("latitude"));
                    assertTrue(coord.containsKey("longitude"));
                    
                    // Verify air pollution data list
                    JsonArray list = responseBody.getJsonArray("list");
                    assertNotNull(list);
                    assertTrue(list.size() > 0);
                    
                    // Verify first air pollution data entry
                    JsonObject airData = list.getJsonObject(0);
                    assertNotNull(airData);
                    
                    assertTrue(airData.containsKey("main"));
                    assertTrue(airData.containsKey("components"));
                    assertTrue(airData.containsKey("dt"));
                    
                    // Verify main AQI data
                    JsonObject main = airData.getJsonObject("main");
                    assertNotNull(main);
                    assertTrue(main.containsKey("aqi"));
                    assertNotNull(main.getInteger("aqi"));
                    
                    // Verify components
                    JsonObject components = airData.getJsonObject("components");
                    assertNotNull(components);
                    assertTrue(components.containsKey("co"));
                    assertTrue(components.containsKey("no"));
                    assertTrue(components.containsKey("no2"));
                    assertTrue(components.containsKey("o3"));
                    assertTrue(components.containsKey("so2"));
                    assertTrue(components.containsKey("pm2_5"));
                    assertTrue(components.containsKey("pm10"));
                    assertTrue(components.containsKey("nh3"));
                    
                    logger.info("Valid coordinates air pollution test passed");
                  });
              testContext.completeNow();
            })
        .onFailure(
            throwable -> {
              logger.error("Valid coordinates air pollution test failed", throwable);
              testContext.failNow(throwable);
            });
  }

  @Test
  void test_special_coordinates_air_pollution(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    String testUrl = AIR_POLLUTION_ENDPOINT + "?latitude=-1&longitude=-1";

    logger.info("Testing air pollution endpoint with special coordinates: {}", testUrl);

    client
        .getAbs(testUrl)
        .send()
        .onSuccess(
            response -> {
              testContext.verify(
                  () -> {
                    assertEquals(200, response.statusCode());
                    
                    JsonObject responseBody = response.bodyAsJsonObject();
                    assertNotNull(responseBody);
                    
                    // Should still return valid structure even for special coordinates
                    assertTrue(responseBody.containsKey("coord"));
                    assertTrue(responseBody.containsKey("list"));
                    
                    logger.info("Special coordinates air pollution test passed");
                  });
              testContext.completeNow();
            })
        .onFailure(
            throwable -> {
              logger.error("Special coordinates air pollution test failed", throwable);
              testContext.failNow(throwable);
            });
  }

  @Test
  void test_missing_latitude_parameter(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    String testUrl = AIR_POLLUTION_ENDPOINT + "?longitude=-79.3839347";

    logger.info("Testing air pollution endpoint with missing latitude: {}", testUrl);

    client
        .getAbs(testUrl)
        .send()
        .onSuccess(
            response -> {
              testContext.verify(
                  () -> {
                    assertEquals(400, response.statusCode());
                    logger.info("Missing latitude parameter test passed - correctly returned 400");
                  });
              testContext.completeNow();
            })
        .onFailure(
            throwable -> {
              logger.error("Missing latitude parameter test failed", throwable);
              testContext.failNow(throwable);
            });
  }

  @Test
  void test_missing_longitude_parameter(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    String testUrl = AIR_POLLUTION_ENDPOINT + "?latitude=43.6534817";

    logger.info("Testing air pollution endpoint with missing longitude: {}", testUrl);

    client
        .getAbs(testUrl)
        .send()
        .onSuccess(
            response -> {
              testContext.verify(
                  () -> {
                    assertEquals(400, response.statusCode());
                    logger.info("Missing longitude parameter test passed - correctly returned 400");
                  });
              testContext.completeNow();
            })
        .onFailure(
            throwable -> {
              logger.error("Missing longitude parameter test failed", throwable);
              testContext.failNow(throwable);
            });
  }

  @Test
  void test_invalid_coordinate_format(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    String testUrl = AIR_POLLUTION_ENDPOINT + "?latitude=invalid&longitude=invalid";

    logger.info("Testing air pollution endpoint with invalid coordinate format: {}", testUrl);

    client
        .getAbs(testUrl)
        .send()
        .onSuccess(
            response -> {
              testContext.verify(
                  () -> {
                    assertEquals(400, response.statusCode());
                    logger.info("Invalid coordinate format test passed - correctly returned 400");
                  });
              testContext.completeNow();
            })
        .onFailure(
            throwable -> {
              logger.error("Invalid coordinate format test failed", throwable);
              testContext.failNow(throwable);
            });
  }

  @Test
  void test_extreme_coordinates(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    String testUrl = AIR_POLLUTION_ENDPOINT + "?latitude=90&longitude=180";

    logger.info("Testing air pollution endpoint with extreme coordinates: {}", testUrl);

    client
        .getAbs(testUrl)
        .send()
        .onSuccess(
            response -> {
              testContext.verify(
                  () -> {
                    // Should handle extreme coordinates gracefully
                    assertTrue(response.statusCode() == 200 || response.statusCode() == 400);
                    
                    if (response.statusCode() == 200) {
                      JsonObject responseBody = response.bodyAsJsonObject();
                      assertNotNull(responseBody);
                      assertTrue(responseBody.containsKey("coord"));
                      assertTrue(responseBody.containsKey("list"));
                    }
                    
                    logger.info("Extreme coordinates test passed with status: {}", response.statusCode());
                  });
              testContext.completeNow();
            })
        .onFailure(
            throwable -> {
              logger.error("Extreme coordinates test failed", throwable);
              testContext.failNow(throwable);
            });
  }
}
