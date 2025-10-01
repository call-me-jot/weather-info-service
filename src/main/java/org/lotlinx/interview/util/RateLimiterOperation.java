package org.lotlinx.interview.util;

import io.vertx.core.Future;

/**
 * Functional interface for operations to be executed with rate limiting.
 */
@FunctionalInterface
public interface RateLimiterOperation<T> {
  Future<T> execute();
}
