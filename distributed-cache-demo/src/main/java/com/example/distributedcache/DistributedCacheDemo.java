package com.example.distributedcache;

import com.example.distributedcache.service.DistributedCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Demo class that showcases distributed cache functionality.
 * Runs various scenarios to demonstrate scalability, availability, and
 * concurrent request handling.
 */
@Component
public class DistributedCacheDemo implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DistributedCacheDemo.class);

    private final DistributedCacheService cacheService;

    public DistributedCacheDemo(DistributedCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("🚀 Starting Distributed Cache Demo...");

        // Wait a bit for the application to fully start
        Thread.sleep(2000);

        // Run various demo scenarios
        basicOperationsDemo();
        performanceDemo();
        concurrentRequestsDemo();
        batchOperationsDemo();
        clusterInfoDemo();

        logger.info("✅ Distributed Cache Demo completed successfully!");
    }

    /**
     * Demonstrates basic cache operations.
     */
    private void basicOperationsDemo() {
        logger.info("=== Basic Operations Demo ===");

        // Store a simple value
        String key1 = "user:123";
        Map<String, Object> userData = Map.of(
                "id", 123,
                "name", "John Doe",
                "email", "john.doe@example.com",
                "createdAt", System.currentTimeMillis());

        boolean stored = cacheService.put(key1, userData, Duration.ofMinutes(30));
        logger.info("Stored user data: {}", stored);

        // Retrieve the value
        Optional<Object> retrieved = cacheService.get(key1);
        if (retrieved.isPresent()) {
            logger.info("Retrieved user data: {}", retrieved.get());
        }

        // Check if key exists
        boolean exists = cacheService.containsKey(key1);
        logger.info("Key exists: {}", exists);

        // Store another value
        String key2 = "config:app";
        Map<String, Object> config = Map.of(
                "version", "1.0.0",
                "environment", "production",
                "features", List.of("cache", "distributed", "scalable"));

        cacheService.put(key2, config);

        // Remove a key
        boolean removed = cacheService.remove(key1);
        logger.info("Removed key: {}", removed);

        // Verify removal
        Optional<Object> afterRemoval = cacheService.get(key1);
        logger.info("After removal, key exists: {}", afterRemoval.isPresent());
    }

    /**
     * Demonstrates performance characteristics.
     */
    private void performanceDemo() {
        logger.info("=== Performance Demo ===");

        int numOperations = 1000;
        Random random = new Random();

        // Measure write performance
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numOperations; i++) {
            String key = "perf:key:" + i;
            String value = "value:" + random.nextInt(100000);
            cacheService.put(key, value, Duration.ofMinutes(5));
        }

        long writeTime = System.currentTimeMillis() - startTime;
        logger.info("Write performance: {} operations in {}ms ({} ops/sec)",
                numOperations, writeTime, (numOperations * 1000.0) / writeTime);

        // Measure read performance
        startTime = System.currentTimeMillis();
        int hits = 0;

        for (int i = 0; i < numOperations; i++) {
            String key = "perf:key:" + i;
            Optional<Object> value = cacheService.get(key);
            if (value.isPresent()) {
                hits++;
            }
        }

        long readTime = System.currentTimeMillis() - startTime;
        logger.info("Read performance: {} hits in {}ms ({} ops/sec)",
                hits, readTime, (hits * 1000.0) / readTime);
    }

    /**
     * Demonstrates concurrent request handling.
     */
    private void concurrentRequestsDemo() {
        logger.info("=== Concurrent Requests Demo ===");

        int numThreads = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int threadId = 0; threadId < numThreads; threadId++) {
            final int thread = threadId;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                Random random = new Random();

                for (int i = 0; i < operationsPerThread; i++) {
                    String key = "concurrent:thread:" + thread + ":key:" + i;
                    String value = "value:" + random.nextInt(100000);

                    // Random operation: 70% reads, 30% writes
                    if (random.nextDouble() < 0.7) {
                        cacheService.get(key);
                    } else {
                        cacheService.put(key, value, Duration.ofMinutes(10));
                    }

                    // Small delay to simulate real-world scenario
                    try {
                        Thread.sleep(random.nextInt(10));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }, executor);

            futures.add(future);
        }

        // Wait for all threads to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        logger.info("Concurrent operations completed successfully!");
    }

    /**
     * Demonstrates batch operations.
     */
    private void batchOperationsDemo() {
        logger.info("=== Batch Operations Demo ===");

        // Prepare batch data
        Map<String, Object> batchData = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            String key = "batch:key:" + i;
            Map<String, Object> value = Map.of(
                    "id", i,
                    "data", "batch-data-" + i,
                    "timestamp", System.currentTimeMillis());
            batchData.put(key, value);
        }

        // Batch write
        long startTime = System.currentTimeMillis();
        Map<String, Boolean> writeResults = cacheService.putMultiple(batchData, Duration.ofMinutes(15));
        long writeTime = System.currentTimeMillis() - startTime;

        long successfulWrites = writeResults.values().stream().filter(Boolean::booleanValue).count();
        logger.info("Batch write: {}/{} successful in {}ms", successfulWrites, batchData.size(), writeTime);

        // Batch read
        List<String> keys = new ArrayList<>(batchData.keySet());
        startTime = System.currentTimeMillis();
        Map<String, Object> readResults = cacheService.getMultiple(keys);
        long readTime = System.currentTimeMillis() - startTime;

        logger.info("Batch read: {}/{} found in {}ms", readResults.size(), keys.size(), readTime);
    }

    /**
     * Demonstrates cluster information and statistics.
     */
    private void clusterInfoDemo() {
        logger.info("=== Cluster Information Demo ===");

        // Get cluster information
        Map<String, Object> clusterInfo = cacheService.getClusterInfo();
        logger.info("Cluster Information:");
        clusterInfo.forEach((key, value) -> logger.info("  {}: {}", key, value));

        // Get cache statistics
        Map<String, Object> stats = cacheService.getStatistics();
        logger.info("Cache Statistics:");
        stats.forEach((key, value) -> {
            if (!"mostAccessedKeys".equals(key)) {
                logger.info("  {}: {}", key, value);
            }
        });

        // Show most accessed keys
        @SuppressWarnings("unchecked")
        List<Map.Entry<String, Long>> mostAccessed = (List<Map.Entry<String, Long>>) stats.get("mostAccessedKeys");
        if (mostAccessed != null && !mostAccessed.isEmpty()) {
            logger.info("Most Accessed Keys:");
            mostAccessed.forEach(entry -> logger.info("  {}: {} accesses", entry.getKey(), entry.getValue()));
        }
    }
}