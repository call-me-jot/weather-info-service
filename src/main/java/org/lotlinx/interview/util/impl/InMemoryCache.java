package org.lotlinx.interview.util.impl;

import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple in-memory cache with TTL (Time To Live) support.
 * Thread-safe implementation using ConcurrentHashMap.
 */
public class InMemoryCache<T> {

  private static final Logger logger = LoggerFactory.getLogger(InMemoryCache.class);

  private final String cacheName;
  private final long ttlMs;
  private final ConcurrentHashMap<String, CacheEntry<T>> cache;
  private final AtomicLong hitCount = new AtomicLong(0);
  private final AtomicLong missCount = new AtomicLong(0);
  private final AtomicLong evictionCount = new AtomicLong(0);

  /**
   * Cache entry with timestamp for TTL calculation.
   */
  private static class CacheEntry<T> {
    private final T value;
    private final long timestamp;

    public CacheEntry(T value) {
      this.value = value;
      this.timestamp = System.currentTimeMillis();
    }

    public T getValue() {
      return value;
    }

    public long getTimestamp() {
      return timestamp;
    }

    public boolean isExpired(long ttlMs) {
      return (System.currentTimeMillis() - timestamp) > ttlMs;
    }
  }

  /**
   * Creates a new in-memory cache.
   *
   * @param cacheName name of the cache for logging
   * @param ttlMs time to live in milliseconds
   * @param vertx Vert.x instance for periodic cleanup
   */
  public InMemoryCache(String cacheName, long ttlMs, Vertx vertx) {
    this.cacheName = cacheName;
    this.ttlMs = ttlMs;
    this.cache = new ConcurrentHashMap<>();
    
    // Schedule periodic cleanup every 5 minutes
    vertx.setPeriodic(300_000, id -> cleanupExpiredEntries());
    
    logger.info("Created cache '{}' with TTL: {}ms", cacheName, ttlMs);
  }

  /**
   * Gets a value from the cache.
   *
   * @param key cache key
   * @return cached value or null if not found or expired
   */
  public T get(String key) {
    CacheEntry<T> entry = cache.get(key);
    
    if (entry == null) {
      missCount.incrementAndGet();
      logger.debug("Cache miss for key: {} in cache: {}", key, cacheName);
      return null;
    }
    
    if (entry.isExpired(ttlMs)) {
      cache.remove(key);
      evictionCount.incrementAndGet();
      missCount.incrementAndGet();
      logger.debug("Cache entry expired for key: {} in cache: {}", key, cacheName);
      return null;
    }
    
    hitCount.incrementAndGet();
    logger.debug("Cache hit for key: {} in cache: {}", key, cacheName);
    return entry.getValue();
  }

  /**
   * Puts a value in the cache.
   *
   * @param key cache key
   * @param value value to cache
   */
  public void put(String key, T value) {
    cache.put(key, new CacheEntry<>(value));
    logger.debug("Cached value for key: {} in cache: {}", key, cacheName);
  }

  /**
   * Removes a value from the cache.
   *
   * @param key cache key
   * @return removed value or null if not found
   */
  public T remove(String key) {
    CacheEntry<T> entry = cache.remove(key);
    if (entry != null) {
      logger.debug("Removed key: {} from cache: {}", key, cacheName);
      return entry.getValue();
    }
    return null;
  }

  /**
   * Clears all entries from the cache.
   */
  public void clear() {
    int size = cache.size();
    cache.clear();
    logger.info("Cleared {} entries from cache: {}", size, cacheName);
  }

  /**
   * Removes expired entries from the cache.
   */
  private void cleanupExpiredEntries() {
    int initialSize = cache.size();
    cache.entrySet().removeIf(entry -> {
      boolean expired = entry.getValue().isExpired(ttlMs);
      if (expired) {
        evictionCount.incrementAndGet();
      }
      return expired;
    });
    
    int removed = initialSize - cache.size();
    if (removed > 0) {
      logger.debug("Cleaned up {} expired entries from cache: {}", removed, cacheName);
    }
  }
}
