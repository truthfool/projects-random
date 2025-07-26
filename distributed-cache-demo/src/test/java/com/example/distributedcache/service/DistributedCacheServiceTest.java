package com.example.distributedcache.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for DistributedCacheService.
 * Tests all major functionality including concurrent operations and edge cases.
 */
@SpringBootTest
@ActiveProfiles("test")
class DistributedCacheServiceTest {

    @Autowired
    private DistributedCacheService cacheService;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private IMap<String, Object> hazelcastMap;

    @BeforeEach
    void setUp() {
        hazelcastMap = hazelcastInstance.getMap("distributed-cache");
        hazelcastMap.clear();

        // Clear Redis test data
        Set<String> keys = redisTemplate.keys("cache:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("Should store and retrieve a simple value")
    void testBasicPutAndGet() {
        String key = "test-key";
        String value = "test-value";

        boolean stored = cacheService.put(key, value);
        assertTrue(stored);

        Optional<Object> retrieved = cacheService.get(key);
        assertTrue(retrieved.isPresent());
        assertEquals(value, retrieved.get());
    }

    @Test
    @DisplayName("Should store and retrieve complex objects")
    void testComplexObjectStorage() {
        String key = "user:123";
        Map<String, Object> userData = Map.of(
                "id", 123,
                "name", "John Doe",
                "email", "john@example.com",
                "active", true,
                "roles", List.of("user", "admin"));

        boolean stored = cacheService.put(key, userData, Duration.ofMinutes(30));
        assertTrue(stored);

        Optional<Object> retrieved = cacheService.get(key);
        assertTrue(retrieved.isPresent());

        @SuppressWarnings("unchecked")
        Map<String, Object> retrievedData = (Map<String, Object>) retrieved.get();
        assertEquals(userData, retrievedData);
    }

    @Test
    @DisplayName("Should handle TTL correctly")
    void testTimeToLive() throws InterruptedException {
        String key = "ttl-test";
        String value = "ttl-value";

        boolean stored = cacheService.put(key, value, Duration.ofMillis(100));
        assertTrue(stored);

        // Value should exist immediately
        Optional<Object> retrieved = cacheService.get(key);
        assertTrue(retrieved.isPresent());
        assertEquals(value, retrieved.get());

        // Wait for TTL to expire
        Thread.sleep(200);

        // Value should be expired
        Optional<Object> expired = cacheService.get(key);
        assertFalse(expired.isPresent());
    }

    @Test
    @DisplayName("Should remove values correctly")
    void testRemove() {
        String key = "remove-test";
        String value = "remove-value";

        cacheService.put(key, value);
        assertTrue(cacheService.containsKey(key));

        boolean removed = cacheService.remove(key);
        assertTrue(removed);

        assertFalse(cacheService.containsKey(key));
        Optional<Object> retrieved = cacheService.get(key);
        assertFalse(retrieved.isPresent());
    }

    @Test
    @DisplayName("Should handle key existence checks")
    void testContainsKey() {
        String key = "exists-test";
        String value = "exists-value";

        assertFalse(cacheService.containsKey(key));

        cacheService.put(key, value);
        assertTrue(cacheService.containsKey(key));

        cacheService.remove(key);
        assertFalse(cacheService.containsKey(key));
    }

    @Test
    @DisplayName("Should handle batch operations")
    void testBatchOperations() {
        Map<String, Object> batchData = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            batchData.put("batch-key-" + i, "batch-value-" + i);
        }

        // Batch put
        Map<String, Boolean> putResults = cacheService.putMultiple(batchData, Duration.ofMinutes(10));
        assertEquals(batchData.size(), putResults.size());
        assertTrue(putResults.values().stream().allMatch(Boolean::booleanValue));

        // Batch get
        List<String> keys = new ArrayList<>(batchData.keySet());
        Map<String, Object> getResults = cacheService.getMultiple(keys);
        assertEquals(batchData.size(), getResults.size());
        assertEquals(batchData, getResults);
    }

    @Test
    @DisplayName("Should handle async operations")
    void testAsyncOperations() throws Exception {
        String key = "async-test";
        String value = "async-value";

        cacheService.put(key, value);

        CompletableFuture<Optional<Object>> future = cacheService.getAsync(key);
        Optional<Object> result = future.get(5, TimeUnit.SECONDS);

        assertTrue(result.isPresent());
        assertEquals(value, result.get());
    }

    @Test
    @DisplayName("Should handle concurrent access")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testConcurrentAccess() throws Exception {
        int numThreads = 10;
        int operationsPerThread = 100;
        AtomicInteger successfulOperations = new AtomicInteger(0);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    String key = "concurrent-" + threadId + "-" + j;
                    String value = "value-" + threadId + "-" + j;

                    try {
                        boolean stored = cacheService.put(key, value);
                        if (stored) {
                            Optional<Object> retrieved = cacheService.get(key);
                            if (retrieved.isPresent() && value.equals(retrieved.get())) {
                                successfulOperations.incrementAndGet();
                            }
                        }
                    } catch (Exception e) {
                        // Ignore exceptions for this test
                    }
                }
            });
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

        // Should have at least 80% success rate
        int totalOperations = numThreads * operationsPerThread;
        double successRate = (double) successfulOperations.get() / totalOperations;
        assertTrue(successRate >= 0.8, "Success rate was: " + successRate);
    }

    @Test
    @DisplayName("Should handle edge cases")
    void testEdgeCases() {
        // Null key
        assertFalse(cacheService.put(null, "value"));
        assertFalse(cacheService.get(null).isPresent());
        assertFalse(cacheService.containsKey(null));
        assertFalse(cacheService.remove(null));

        // Empty key
        assertFalse(cacheService.put("", "value"));
        assertFalse(cacheService.get("").isPresent());
        assertFalse(cacheService.containsKey(""));
        assertFalse(cacheService.remove(""));

        // Null value
        assertTrue(cacheService.put("null-value-key", null));
        Optional<Object> retrieved = cacheService.get("null-value-key");
        assertTrue(retrieved.isPresent());
        assertNull(retrieved.get());
    }

    @Test
    @DisplayName("Should provide accurate statistics")
    void testStatistics() {
        // Perform some operations
        for (int i = 0; i < 10; i++) {
            cacheService.put("stats-key-" + i, "stats-value-" + i);
        }

        for (int i = 0; i < 5; i++) {
            cacheService.get("stats-key-" + i);
        }

        // Get some non-existent keys
        for (int i = 10; i < 15; i++) {
            cacheService.get("non-existent-" + i);
        }

        Map<String, Object> stats = cacheService.getStatistics();

        assertNotNull(stats.get("totalRequests"));
        assertNotNull(stats.get("cacheHits"));
        assertNotNull(stats.get("cacheMisses"));
        assertNotNull(stats.get("hitRate"));
        assertNotNull(stats.get("hazelcastSize"));
        assertNotNull(stats.get("redisSize"));
        assertNotNull(stats.get("clusterSize"));
        assertNotNull(stats.get("mostAccessedKeys"));

        long totalRequests = (Long) stats.get("totalRequests");
        long cacheHits = (Long) stats.get("cacheHits");
        long cacheMisses = (Long) stats.get("cacheMisses");

        assertEquals(totalRequests, cacheHits + cacheMisses);
    }

    @Test
    @DisplayName("Should provide cluster information")
    void testClusterInfo() {
        Map<String, Object> clusterInfo = cacheService.getClusterInfo();

        assertNotNull(clusterInfo.get("clusterName"));
        assertNotNull(clusterInfo.get("instanceName"));
        assertNotNull(clusterInfo.get("memberCount"));
        assertNotNull(clusterInfo.get("partitionCount"));
        assertNotNull(clusterInfo.get("localMember"));

        int memberCount = (Integer) clusterInfo.get("memberCount");
        assertTrue(memberCount > 0);
    }

    @Test
    @DisplayName("Should clear cache completely")
    void testClear() {
        // Add some data
        for (int i = 0; i < 5; i++) {
            cacheService.put("clear-key-" + i, "clear-value-" + i);
        }

        // Verify data exists
        for (int i = 0; i < 5; i++) {
            assertTrue(cacheService.containsKey("clear-key-" + i));
        }

        // Clear cache
        boolean cleared = cacheService.clear();
        assertTrue(cleared);

        // Verify data is gone
        for (int i = 0; i < 5; i++) {
            assertFalse(cacheService.containsKey("clear-key-" + i));
        }

        // Check statistics are reset
        Map<String, Object> stats = cacheService.getStatistics();
        assertEquals(0L, stats.get("totalRequests"));
        assertEquals(0L, stats.get("cacheHits"));
        assertEquals(0L, stats.get("cacheMisses"));
    }

    @RepeatedTest(5)
    @DisplayName("Should maintain consistency across repeated operations")
    void testConsistency() {
        String key = "consistency-test";
        String value = "consistency-value";

        // Store value
        assertTrue(cacheService.put(key, value));

        // Retrieve multiple times
        for (int i = 0; i < 10; i++) {
            Optional<Object> retrieved = cacheService.get(key);
            assertTrue(retrieved.isPresent());
            assertEquals(value, retrieved.get());
        }

        // Remove and verify
        assertTrue(cacheService.remove(key));
        assertFalse(cacheService.containsKey(key));
    }

    @Test
    @DisplayName("Should handle large objects")
    void testLargeObjectStorage() {
        String key = "large-object";

        // Create a large object
        Map<String, Object> largeObject = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            largeObject.put("key-" + i, "value-" + i + "-".repeat(100));
        }

        boolean stored = cacheService.put(key, largeObject, Duration.ofMinutes(5));
        assertTrue(stored);

        Optional<Object> retrieved = cacheService.get(key);
        assertTrue(retrieved.isPresent());

        @SuppressWarnings("unchecked")
        Map<String, Object> retrievedObject = (Map<String, Object>) retrieved.get();
        assertEquals(largeObject.size(), retrievedObject.size());
        assertEquals(largeObject, retrievedObject);
    }
}