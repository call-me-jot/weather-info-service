package org.lotlinx.interview.util;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility class for making HTTP requests. */
public class HttpClientUtil implements AutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

  private final Vertx vertx;
  private final WebClient webClient;

  public HttpClientUtil(Vertx vertx) {
    this.vertx = vertx;
    this.webClient = WebClient.create(vertx);
  }

  /**
   * Sends a GET request to the specified host and path.
   *
   * @param host the target host
   * @param path the request path
   * @param queryParams query parameters
   * @param port the target port
   * @return Future containing the response as JsonObject
   */
  public Future<JsonObject> sendGetRequest(
      String host, String path, MultiMap queryParams, int port) {
    return sendGetRequest(host, path, queryParams, port, null);
  }

  /**
   * Sends a GET request to the specified host and path with headers.
   *
   * @param host the target host
   * @param path the request path
   * @param queryParams query parameters
   * @param port the target port
   * @param headers optional headers
   * @return Future containing the response as JsonObject
   */
  public Future<JsonObject> sendGetRequest(
      String host, String path, MultiMap queryParams, int port, MultiMap headers) {
    Promise<JsonObject> promise = Promise.promise();

    logger.debug("Sending GET request to {}:{}{}", host, port, path);

    HttpRequest<Buffer> request = webClient.get(port, host, path).ssl(true);

    // Add headers if provided
    if (headers != null && !headers.isEmpty()) {
      request = request.putHeaders(headers);
    }

    // Add query parameters
    if (queryParams != null && !queryParams.isEmpty()) {
      for (var entry : queryParams.entries()) {
        request = request.setQueryParam(entry.getKey(), entry.getValue());
      }
    }

    request.send(
        ar -> {
          if (ar.succeeded()) {
            try {
              String responseBody = ar.result().bodyAsString();
              logger.debug("Received response: {}", responseBody);

              JsonObject jsonResponse = new JsonObject(responseBody);
              promise.complete(jsonResponse);
            } catch (Exception e) {
              logger.error("Failed to parse response as JSON", e);
              promise.fail(new RuntimeException("Failed to parse JSON response: " + e.getMessage(), e));
            }
          } else {
            logger.error("HTTP request failed", ar.cause());
            promise.fail(ar.cause());
          }
        });

    return promise.future();
  }

  /**
   * Sends a GET request and returns the raw response body as string.
   *
   * @param host the target host
   * @param path the request path
   * @param queryParams query parameters
   * @param port the target port
   * @return Future containing the response as String
   */
  public Future<String> sendGetRequestRaw(
      String host, String path, MultiMap queryParams, int port) {
    Promise<String> promise = Promise.promise();

    logger.debug("Sending GET request to {}:{}{}", host, port, path);

    HttpRequest<Buffer> request = webClient.get(port, host, path).ssl(true);

    // Add query parameters
    if (queryParams != null && !queryParams.isEmpty()) {
      for (var entry : queryParams.entries()) {
        request = request.setQueryParam(entry.getKey(), entry.getValue());
      }
    }

    request.send(
        ar -> {
          if (ar.succeeded()) {
            try {
              String responseBody = ar.result().bodyAsString();
              logger.debug("Received raw response: {}", responseBody);
              promise.complete(responseBody);
            } catch (Exception e) {
              logger.error("Failed to get response body", e);
              promise.fail(new RuntimeException("Failed to get response body: " + e.getMessage(), e));
            }
          } else {
            logger.error("HTTP request failed", ar.cause());
            promise.fail(ar.cause());
          }
        });

    return promise.future();
  }

  /**
   * Closes the WebClient to free resources.
   * This method should be called when the HttpClientUtil is no longer needed.
   */
  @Override
  public void close() {
    if (webClient != null) {
      webClient.close();
      logger.debug("WebClient closed successfully");
    }
  }
}
