package org.lotlinx.interview;

import io.vertx.core.Vertx;
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
        .send(
            testContext.succeeding(
                response -> {
                  testContext.verify(
                      () -> {
                        assertEquals(200, response.statusCode());
                        JsonObject responseBody = response.bodyAsJsonObject();
                        
                        assertNotNull(responseBody);
                        assertTrue(responseBody.containsKey("coord"));
                        assertTrue(responseBody.containsKey("list"));
                        
                        JsonObject coord = responseBody.getJsonObject("coord");
                        assertNotNull(coord);
                        assertTrue(coord.containsKey("latitude"));
                        assertTrue(coord.containsKey("longitude"));
                        
                        logger.info("Valid coordinates air pollution test passed");
                      });
                  testContext.completeNow();
                }));
  }

  @Test
  void test_special_coordinates_air_pollution(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    String testUrl = AIR_POLLUTION_ENDPOINT + "?latitude=-1&longitude=-1";

    logger.info("Testing air pollution endpoint with special coordinates: {}", testUrl);

    client
        .getAbs(testUrl)
        .send(
            testContext.succeeding(
                response -> {
                  testContext.verify(
                      () -> {
                        // According to README.md, -1,-1 coordinates should be allowed
                        assertEquals(200, response.statusCode());
                        JsonObject responseBody = response.bodyAsJsonObject();
                        assertNotNull(responseBody);
                        
                        logger.info("Special coordinates (-1,-1) test passed");
                      });
                  testContext.completeNow();
                }));
  }

  @Test
  void test_missing_latitude_parameter(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    String testUrl = AIR_POLLUTION_ENDPOINT + "?longitude=-79.3839347";

    logger.info("Testing air pollution endpoint with missing latitude: {}", testUrl);

    client
        .getAbs(testUrl)
        .send(
            testContext.succeeding(
                response -> {
                  testContext.verify(
                      () -> {
                        assertEquals(400, response.statusCode());
                        JsonObject responseBody = response.bodyAsJsonObject();
                        assertNotNull(responseBody);
                        assertTrue(responseBody.containsKey("error"));
                        assertTrue(responseBody.getString("message").contains("latitude"));
                        
                        logger.info("Missing latitude parameter test passed");
                      });
                  testContext.completeNow();
                }));
  }

  @Test
  void test_missing_longitude_parameter(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    String testUrl = AIR_POLLUTION_ENDPOINT + "?latitude=43.6534817";

    logger.info("Testing air pollution endpoint with missing longitude: {}", testUrl);

    client
        .getAbs(testUrl)
        .send(
            testContext.succeeding(
                response -> {
                  testContext.verify(
                      () -> {
                        assertEquals(400, response.statusCode());
                        JsonObject responseBody = response.bodyAsJsonObject();
                        assertNotNull(responseBody);
                        assertTrue(responseBody.containsKey("error"));
                        assertTrue(responseBody.getString("message").contains("longitude"));
                        
                        logger.info("Missing longitude parameter test passed");
                      });
                  testContext.completeNow();
                }));
  }

  @Test
  void test_invalid_coordinate_format(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    String testUrl = AIR_POLLUTION_ENDPOINT + "?latitude=invalid&longitude=invalid";

    logger.info("Testing air pollution endpoint with invalid coordinate format: {}", testUrl);

    client
        .getAbs(testUrl)
        .send(
            testContext.succeeding(
                response -> {
                  testContext.verify(
                      () -> {
                        assertEquals(400, response.statusCode());
                        JsonObject responseBody = response.bodyAsJsonObject();
                        assertNotNull(responseBody);
                        assertTrue(responseBody.containsKey("error"));
                        assertTrue(responseBody.getString("message").contains("Invalid number format"));
                        
                        logger.info("Invalid coordinate format test passed");
                      });
                  testContext.completeNow();
                }));
  }

  @Test
  void test_extreme_coordinates(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    String testUrl = AIR_POLLUTION_ENDPOINT + "?latitude=90&longitude=180";

    logger.info("Testing air pollution endpoint with extreme coordinates: {}", testUrl);

    client
        .getAbs(testUrl)
        .send(
            testContext.succeeding(
                response -> {
                  testContext.verify(
                      () -> {
                        // Extreme coordinates should be handled by the API
                        assertTrue(response.statusCode() == 200 || response.statusCode() == 400);
                        
                        if (response.statusCode() == 200) {
                          JsonObject responseBody = response.bodyAsJsonObject();
                          assertNotNull(responseBody);
                          logger.info("Extreme coordinates accepted by API");
                        } else {
                          logger.info("Extreme coordinates rejected by API (expected behavior)");
                        }
                      });
                  testContext.completeNow();
                }));
  }

  @Test
  void test_boundary_coordinates(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    String testUrl = AIR_POLLUTION_ENDPOINT + "?latitude=0&longitude=0";

    logger.info("Testing air pollution endpoint with boundary coordinates (0,0): {}", testUrl);

    client
        .getAbs(testUrl)
        .send(
            testContext.succeeding(
                response -> {
                  testContext.verify(
                      () -> {
                        assertEquals(200, response.statusCode());
                        JsonObject responseBody = response.bodyAsJsonObject();
                        assertNotNull(responseBody);
                        
                        logger.info("Boundary coordinates (0,0) test passed");
                      });
                  testContext.completeNow();
                }));
  }

  @Test
  void test_negative_coordinates(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    String testUrl = AIR_POLLUTION_ENDPOINT + "?latitude=-33.8688&longitude=151.2093"; // Sydney coordinates

    logger.info("Testing air pollution endpoint with negative coordinates: {}", testUrl);

    client
        .getAbs(testUrl)
        .send(
            testContext.succeeding(
                response -> {
                  testContext.verify(
                      () -> {
                        assertEquals(200, response.statusCode());
                        JsonObject responseBody = response.bodyAsJsonObject();
                        assertNotNull(responseBody);
                        
                        logger.info("Negative coordinates test passed");
                      });
                  testContext.completeNow();
                }));
  }
}
