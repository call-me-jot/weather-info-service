package org.lotlinx.interview.util;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Circuit breaker implementation for handling external API calls.
 * Provides resilience patterns: CLOSED, OPEN, HALF_OPEN states.
 */
public class CircuitBreaker {

  private static final Logger logger = LoggerFactory.getLogger(CircuitBreaker.class);

  public enum State {
    CLOSED,    // Normal operation, requests pass through
    OPEN,      // Circuit is open, requests fail fast
    HALF_OPEN  // Testing if service is back, limited requests allowed
  }

  private final String name;
  private final int failureThreshold;
  private final long timeoutMs;
  private final long retryTimeoutMs;
  private final Vertx vertx;

  private volatile State state = State.CLOSED;
  private final AtomicInteger failureCount = new AtomicInteger(0);
  private final AtomicLong lastFailureTime = new AtomicLong(0);
  private final AtomicInteger halfOpenSuccessCount = new AtomicInteger(0);

  public CircuitBreaker(String name, int failureThreshold, long timeoutMs, long retryTimeoutMs, Vertx vertx) {
    this.name = name;
    this.failureThreshold = failureThreshold;
    this.timeoutMs = timeoutMs;
    this.retryTimeoutMs = retryTimeoutMs;
    this.vertx = vertx;
  }

  /**
   * Executes an operation through the circuit breaker.
   */
  public <T> Future<T> execute(Operation<T> operation) {
    Promise<T> promise = Promise.promise();

    if (state == State.OPEN) {
      if (shouldAttemptReset()) {
        state = State.HALF_OPEN;
        halfOpenSuccessCount.set(0);
        logger.info("Circuit breaker [{}] transitioning to HALF_OPEN", name);
      } else {
        logger.warn("Circuit breaker [{}] is OPEN, rejecting request", name);
        promise.fail(new CircuitBreakerOpenException("Circuit breaker is open"));
        return promise.future();
      }
    }

    if (state == State.HALF_OPEN && halfOpenSuccessCount.get() >= 3) {
      state = State.CLOSED;
      failureCount.set(0);
      logger.info("Circuit breaker [{}] transitioning to CLOSED after successful half-open tests", name);
    }

    // Execute the operation with timeout
    Future<T> operationFuture = operation.execute();
    
    // Set timeout
    vertx.setTimer(timeoutMs, timerId -> {
      if (!operationFuture.isComplete()) {
        operationFuture.cause();
        handleFailure(promise, new CircuitBreakerTimeoutException("Operation timed out"));
      }
    });

    operationFuture
        .onSuccess(result -> {
          handleSuccess();
          promise.complete(result);
        })
        .onFailure(throwable -> {
          handleFailure(promise, throwable);
        });

    return promise.future();
  }

  private void handleSuccess() {
    if (state == State.HALF_OPEN) {
      halfOpenSuccessCount.incrementAndGet();
      logger.debug("Circuit breaker [{}] half-open success count: {}", name, halfOpenSuccessCount.get());
    } else {
      failureCount.set(0);
    }
  }

  private void handleFailure(Promise<?> promise, Throwable throwable) {
    int currentFailures = failureCount.incrementAndGet();
    lastFailureTime.set(Instant.now().toEpochMilli());

    logger.warn("Circuit breaker [{}] failure count: {}/{}", name, currentFailures, failureThreshold);

    if (currentFailures >= failureThreshold) {
      state = State.OPEN;
      logger.error("Circuit breaker [{}] transitioning to OPEN after {} failures", name, currentFailures);
    }

    promise.fail(throwable);
  }

  private boolean shouldAttemptReset() {
    return Instant.now().toEpochMilli() - lastFailureTime.get() >= retryTimeoutMs;
  }

  public State getState() {
    return state;
  }

  public int getFailureCount() {
    return failureCount.get();
  }

  public String getName() {
    return name;
  }

  /**
   * Functional interface for operations to be executed through the circuit breaker.
   */
  @FunctionalInterface
  public interface Operation<T> {
    Future<T> execute();
  }

  /**
   * Exception thrown when circuit breaker is open.
   */
  public static class CircuitBreakerOpenException extends RuntimeException {
    public CircuitBreakerOpenException(String message) {
      super(message);
    }
  }

  /**
   * Exception thrown when operation times out.
   */
  public static class CircuitBreakerTimeoutException extends RuntimeException {
    public CircuitBreakerTimeoutException(String message) {
      super(message);
    }
  }
}
