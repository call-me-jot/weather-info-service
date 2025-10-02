package org.lotlinx.interview;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class HelloTest {

  private static final Logger logger = LoggerFactory.getLogger(HelloTest.class);

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    logger.info("Deploying MainServerVerticle for hello testing");
    vertx.deployVerticle(
        new MainServerVerticle(),
        testContext.succeeding(
            id -> {
              logger.info("MainServerVerticle deployed successfully with ID: {}", id);
              testContext.completeNow();
            }));
  }

  @Test
  void get_hello_endpoint(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    String testUrl = "http://localhost:8080/hello";

    logger.info("Testing hello endpoint: {}", testUrl);

    client
        .getAbs(testUrl)
        .send(
            testContext.succeeding(
                response -> {
                  testContext.verify(
                      () -> {
                        assertEquals(200, response.statusCode());
                        assertEquals("Hello from Lotlinx!", response.bodyAsString());
                        logger.info("Hello endpoint test passed: {}", response.bodyAsString());
                      });
                  testContext.completeNow();
                }));
  }
}