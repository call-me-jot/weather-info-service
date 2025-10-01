package org.lotlinx.interview;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.lotlinx.interview.config.ApplicationConfig;
import org.lotlinx.interview.router.ApiRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Main Verticle that starts the HTTP server and configures routing. */
public class MainServerVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(MainServerVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) {
    logger.info("Starting MainServerVerticle...");

    try {
      // Create API router
      ApiRouter apiRouter = new ApiRouter(vertx);
      Router router = apiRouter.createRouter();

      // Create and start HTTP server
      vertx
          .createHttpServer()
          .requestHandler(router)
          .listen(ApplicationConfig.SERVER_PORT, ApplicationConfig.SERVER_HOST)
          .onSuccess(
              server -> {
                logger.info(
                    "HTTP server started successfully on {}:{}",
                    ApplicationConfig.SERVER_HOST,
                    ApplicationConfig.SERVER_PORT);
                startPromise.complete();
              })
          .onFailure(
              throwable -> {
                logger.error("Failed to start HTTP server", throwable);
                startPromise.fail(throwable);
              });

    } catch (Exception e) {
      logger.error("Error during verticle startup", e);
      startPromise.fail(e);
    }
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    logger.info("Stopping MainServerVerticle...");
    stopPromise.complete();
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx
        .deployVerticle(new MainServerVerticle())
        .onSuccess(id -> logger.info("MainServerVerticle deployed successfully with ID: {}", id))
        .onFailure(
            throwable -> {
              logger.error("Failed to deploy MainServerVerticle", throwable);
              System.exit(1);
            });
  }
}
