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
public class ApiRouter {

  private static final Logger logger = LoggerFactory.getLogger(ApiRouter.class);

  private final Vertx vertx;
  private final WeatherController weatherController;

  public ApiRouter(Vertx vertx) {
    this.vertx = vertx;
    WeatherService weatherService = new OpenWeatherService(vertx);
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
    router.get(ApplicationConfig.getHelloEndpoint()).handler(weatherController::handleHello);

    // Air pollution endpoint
    router
        .get(ApplicationConfig.getAirPollutionEndpoint())
        .handler(weatherController::handleAirPollution);

    // Multi-city weather endpoint
    router
        .post(ApplicationConfig.getMultiCityWeatherEndpoint())
        .handler(weatherController::handleMultiCityWeather);

    logger.info(
        "Routes configured: {}, {}, {}",
        ApplicationConfig.getHelloEndpoint(),
        ApplicationConfig.getAirPollutionEndpoint(),
        ApplicationConfig.getMultiCityWeatherEndpoint());
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
}
