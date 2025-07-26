package com.example.distributedcache.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Core service for distributed caching operations.
 * Provides high-performance caching with both Hazelcast (in-memory) and Redis
 * (persistent) layers.
 */
@Service
public class DistributedCacheService {

    private static final Logger logger = LoggerFactory.getLogger(DistributedCacheService.class);
    private static final String CACHE_NAME = "distributed-cache";
    private static final String REDIS_CACHE_PREFIX = "cache:";

    private final HazelcastInstance hazelcastInstance;
    private final RedisTemplate<String, Object> redisTemplate;
    private final IMap<String, Object> hazelcastMap;

    // Local statistics tracking
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final Map<String, Long> keyAccessCount = new ConcurrentHashMap<>();

    @Autowired
    public DistributedCacheService(HazelcastInstance hazelcastInstance,
            RedisTemplate<String, Object> redisTemplate) {
        this.hazelcastInstance = hazelcastInstance;
        this.redisTemplate = redisTemplate;
        this.hazelcastMap = hazelcastInstance.getMap(CACHE_NAME);

        logger.info("DistributedCacheService initialized with Hazelcast instance: {}",
                hazelcastInstance.getName());
    }

    /**
     * Stores a value in the distributed cache with TTL.
     */
    public boolean put(String key, Object value, Duration ttl) {
        try {
            totalRequests.incrementAndGet();
            keyAccessCount.merge(key, 1L, Long::sum);

            // Store in Hazelcast (primary cache)
            hazelcastMap.set(key, value, ttl.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);

            // Store in Redis (persistent backup)
            String redisKey = REDIS_CACHE_PREFIX + key;
            redisTemplate.opsForValue().set(redisKey, value, ttl);

            logger.debug("Stored key: {} in distributed cache", key);
            return true;
        } catch (Exception e) {
            logger.error("Error storing key: {}", key, e);
            return false;
        }
    }

    /**
     * Stores a value in the distributed cache with default TTL.
     */
    public boolean put(String key, Object value) {
        return put(key, value, Duration.ofHours(1));
    }

    /**
     * Retrieves a value from the distributed cache.
     * Implements multi-level caching: Hazelcast -> Redis -> null
     */
    public Optional<Object> get(String key) {
        try {
            totalRequests.incrementAndGet();
            keyAccessCount.merge(key, 1L, Long::sum);

            // Try Hazelcast first (fastest)
            Object value = hazelcastMap.get(key);
            if (value != null) {
                cacheHits.incrementAndGet();
                logger.debug("Cache hit in Hazelcast for key: {}", key);
                return Optional.of(value);
            }

            // Try Redis if not in Hazelcast
            String redisKey = REDIS_CACHE_PREFIX + key;
            value = redisTemplate.opsForValue().get(redisKey);
            if (value != null) {
                cacheHits.incrementAndGet();
                // Populate Hazelcast with the value from Redis
                hazelcastMap.set(key, value, Duration.ofHours(1).toMillis(),
                        java.util.concurrent.TimeUnit.MILLISECONDS);
                logger.debug("Cache hit in Redis for key: {}, populated Hazelcast", key);
                return Optional.of(value);
            }

            cacheMisses.incrementAndGet();
            logger.debug("Cache miss for key: {}", key);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error retrieving key: {}", key, e);
            return Optional.empty();
        }
    }

    /**
     * Asynchronously retrieves a value from the distributed cache.
     */
    @Async("cacheTaskExecutor")
    public CompletableFuture<Optional<Object>> getAsync(String key) {
        return CompletableFuture.completedFuture(get(key));
    }

    /**
     * Removes a value from the distributed cache.
     */
    public boolean remove(String key) {
        try {
            totalRequests.incrementAndGet();

            // Remove from both caches
            Object removedFromHazelcast = hazelcastMap.remove(key);
            String redisKey = REDIS_CACHE_PREFIX + key;
            Boolean removedFromRedis = redisTemplate.delete(redisKey);

            boolean removed = removedFromHazelcast != null || Boolean.TRUE.equals(removedFromRedis);
            logger.debug("Removed key: {} from distributed cache", key);
            return removed;
        } catch (Exception e) {
            logger.error("Error removing key: {}", key, e);
            return false;
        }
    }

    /**
     * Checks if a key exists in the distributed cache.
     */
    public boolean containsKey(String key) {
        try {
            totalRequests.incrementAndGet();

            // Check Hazelcast first
            if (hazelcastMap.containsKey(key)) {
                return true;
            }

            // Check Redis
            String redisKey = REDIS_CACHE_PREFIX + key;
            return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
        } catch (Exception e) {
            logger.error("Error checking key existence: {}", key, e);
            return false;
        }
    }

    /**
     * Retrieves multiple values in batch.
     */
    public Map<String, Object> getMultiple(List<String> keys) {
        Map<String, Object> result = new HashMap<>();

        for (String key : keys) {
            get(key).ifPresent(value -> result.put(key, value));
        }

        return result;
    }

    /**
     * Stores multiple values in batch.
     */
    public Map<String, Boolean> putMultiple(Map<String, Object> keyValuePairs, Duration ttl) {
        Map<String, Boolean> results = new HashMap<>();

        for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
            boolean success = put(entry.getKey(), entry.getValue(), ttl);
            results.put(entry.getKey(), success);
        }

        return results;
    }

    /**
     * Clears all data from the distributed cache.
     */
    public boolean clear() {
        try {
            hazelcastMap.clear();
            Set<String> keys = redisTemplate.keys(REDIS_CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }

            // Reset statistics
            totalRequests.set(0);
            cacheHits.set(0);
            cacheMisses.set(0);
            keyAccessCount.clear();

            logger.info("Distributed cache cleared successfully");
            return true;
        } catch (Exception e) {
            logger.error("Error clearing distributed cache", e);
            return false;
        }
    }

    /**
     * Gets cache statistics.
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long total = totalRequests.get();
        long hits = cacheHits.get();
        long misses = cacheMisses.get();

        stats.put("totalRequests", total);
        stats.put("cacheHits", hits);
        stats.put("cacheMisses", misses);
        stats.put("hitRate", total > 0 ? (double) hits / total : 0.0);
        stats.put("hazelcastSize", hazelcastMap.size());
        stats.put("redisSize", getRedisSize());
        stats.put("clusterSize", hazelcastInstance.getCluster().getMembers().size());
        stats.put("mostAccessedKeys", getMostAccessedKeys(10));

        return stats;
    }

    /**
     * Gets the size of Redis cache.
     */
    private long getRedisSize() {
        try {
            Set<String> keys = redisTemplate.keys(REDIS_CACHE_PREFIX + "*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            logger.error("Error getting Redis size", e);
            return 0;
        }
    }

    /**
     * Gets the most accessed keys.
     */
    private List<Map.Entry<String, Long>> getMostAccessedKeys(int limit) {
        return keyAccessCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .toList();
    }

    /**
     * Gets cluster information.
     */
    public Map<String, Object> getClusterInfo() {
        Map<String, Object> clusterInfo = new HashMap<>();

        clusterInfo.put("clusterName", hazelcastInstance.getConfig().getClusterName());
        clusterInfo.put("instanceName", hazelcastInstance.getName());
        clusterInfo.put("memberCount", hazelcastInstance.getCluster().getMembers().size());
        clusterInfo.put("partitionCount", 271); // Default Hazelcast partition count
        clusterInfo.put("localMember", hazelcastInstance.getCluster().getLocalMember().getAddress().toString());

        return clusterInfo;
    }
}