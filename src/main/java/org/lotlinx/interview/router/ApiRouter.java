package org.lotlinx.interview.router;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.lotlinx.interview.config.ApplicationConfig;
import org.lotlinx.interview.controller.WeatherController;
import org.lotlinx.interview.service.WeatherService;
import org.lotlinx.interview.service.impl.OpenWeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Router configuration for API endpoints. */
public class ApiRouter implements AutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(ApiRouter.class);

  private final Vertx vertx;
  private final WeatherController weatherController;
  private final WeatherService weatherService;

  public ApiRouter(Vertx vertx) {
    this.vertx = vertx;
    this.weatherService = new OpenWeatherService(vertx);
    this.weatherController = new WeatherController(weatherService);
  }

  /**
   * Creates and configures the main API router.
   *
   * @return configured Router instance
   */
  public Router createRouter() {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    // Configure routes
    setupRoutes(router);

    // Add error handling
    setupErrorHandling(router);

    logger.info("API router configured successfully");
    return router;
  }

  /** Sets up all API routes. */
  private void setupRoutes(Router router) {
    // Health check endpoint
    router.get(ApplicationConfig.HELLO_ENDPOINT).handler(weatherController::handleHello);

    // Air pollution endpoint
    router
        .get(ApplicationConfig.AIR_POLLUTION_ENDPOINT)
        .handler(weatherController::handleAirPollution);

    // Multi-city weather endpoint
    router
        .post(ApplicationConfig.MULTI_CITY_WEATHER_ENDPOINT)
        .handler(weatherController::handleMultiCityWeather);

    logger.debug(
        "Routes configured: {}, {}, {}",
        ApplicationConfig.HELLO_ENDPOINT,
        ApplicationConfig.AIR_POLLUTION_ENDPOINT,
        ApplicationConfig.MULTI_CITY_WEATHER_ENDPOINT);
  }

  /** Sets up global error handling for the router. */
  private void setupErrorHandling(Router router) {
    // Handle 404 - Not Found
    router
        .route()
        .last()
        .handler(
            ctx -> {
              ctx.response()
                  .setStatusCode(404)
                  .putHeader("content-type", "application/json")
                  .end(
                      "{\"error\":\"Not Found\",\"message\":\"The requested resource was not found\"}");
            });

    // Handle uncaught exceptions
    router.errorHandler(
        500,
        ctx -> {
          logger.error("Unhandled exception in router", ctx.failure());
          ctx.response()
              .setStatusCode(500)
              .putHeader("content-type", "application/json")
              .end(
                  "{\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\"}");
        });
  }

  /**
   * Closes the WeatherService to free resources.
   * This method should be called when the ApiRouter is no longer needed.
   */
  @Override
  public void close() {
    if (weatherService instanceof AutoCloseable) {
      try {
        ((AutoCloseable) weatherService).close();
        logger.debug("ApiRouter closed successfully");
      } catch (Exception e) {
        logger.warn("Error closing WeatherService", e);
      }
    }
  }
}
