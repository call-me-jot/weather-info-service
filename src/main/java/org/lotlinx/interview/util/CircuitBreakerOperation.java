package org.lotlinx.interview.util;

import io.vertx.core.Future;

/**
 * Functional interface for operations to be executed through the circuit breaker.
 */
@FunctionalInterface
public interface CircuitBreakerOperation<T> {
  Future<T> execute();
}
