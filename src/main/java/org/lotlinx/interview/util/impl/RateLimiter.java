package org.lotlinx.interview.util.impl;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.lotlinx.interview.util.RateLimiterOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiter implementation for API call management.
 * Tracks daily API usage and enforces rate limits for free tier APIs.
 */
public class RateLimiter {

  private static final Logger logger = LoggerFactory.getLogger(RateLimiter.class);

  private final String name;
  private final int dailyLimit;
  private final Vertx vertx;
  private final ConcurrentHashMap<LocalDate, AtomicInteger> dailyUsage = new ConcurrentHashMap<>();
  private final AtomicInteger currentDayUsage = new AtomicInteger(0);
  private LocalDate currentDate = LocalDate.now();

  public RateLimiter(String name, int dailyLimit, Vertx vertx) {
    this.name = name;
    this.dailyLimit = dailyLimit;
    this.vertx = vertx;
    
    // Schedule daily reset
    scheduleDailyReset();
  }

  /**
   * Checks if a request can be made within the rate limit.
   * 
   * @return true if request is allowed, false if rate limit exceeded
   */
  public boolean isRequestAllowed() {
    LocalDate today = LocalDate.now();
    
    // Reset if it's a new day
    if (!today.equals(currentDate)) {
      resetDailyUsage();
      currentDate = today;
    }
    
    int currentUsage = currentDayUsage.get();
    boolean allowed = currentUsage < dailyLimit;

      if (!allowed) {
      logger.warn("Rate limit exceeded for [{}]. Current usage: {}/{}", name, currentUsage, dailyLimit);
    }
    
    return allowed;
  }

  /**
   * Records a successful API call.
   * 
   * @return the current usage count after incrementing
   */
  public int recordApiCall() {
    LocalDate today = LocalDate.now();
    
    // Reset if it's a new day
    if (!today.equals(currentDate)) {
      resetDailyUsage();
      currentDate = today;
    }
    
    int newUsage = currentDayUsage.incrementAndGet();
    dailyUsage.computeIfAbsent(today, k -> new AtomicInteger(0)).incrementAndGet();
    
    logger.debug("API call recorded for [{}]. Usage: {}/{}", name, newUsage, dailyLimit);
    
    // Log warning when approaching limit
    if (newUsage >= dailyLimit * 0.8) {
      logger.warn("Approaching rate limit for [{}]. Usage: {}/{} ({}% used)", 
          name, newUsage, dailyLimit, (newUsage * 100 / dailyLimit));
    }
    
    return newUsage;
  }

  /**
   * Executes an operation with rate limiting.
   * 
   * @param operation the operation to execute
   * @return Future that completes with the result or fails with rate limit exceeded
   */
  public <T> Future<T> executeWithRateLimit(RateLimiterOperation<T> operation) {
    if (!isRequestAllowed()) {
      return Future.failedFuture(new RateLimitExceededException(
          "Rate limit exceeded for " + name + ". Daily limit: " + dailyLimit));
    }
    
    return operation.execute()
        .onSuccess(result -> recordApiCall())
        .onFailure(throwable -> {
          // Don't record failed calls as they don't count against rate limit
          logger.debug("API call failed for [{}], not counting against rate limit", name);
        });
  }

  /**
   * Resets daily usage counter.
   */
  private void resetDailyUsage() {
    int previousUsage = currentDayUsage.getAndSet(0);
    if (previousUsage > 0) {
      logger.info("Daily usage reset for [{}]. Previous usage: {}", name, previousUsage);
    }
  }

  /**
   * Schedules daily reset at midnight.
   */
  private void scheduleDailyReset() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime midnight = now.toLocalDate().plusDays(1).atTime(LocalTime.MIDNIGHT);
    
    long delayMs = java.time.Duration.between(now, midnight).toMillis();
    
    vertx.setTimer(delayMs, timerId -> {
      resetDailyUsage();
      currentDate = LocalDate.now();
      
      // Schedule next reset
      scheduleDailyReset();
    });
  }


  /**
   * Exception thrown when rate limit is exceeded.
   */
  public static class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
      super(message);
    }
  }
}
