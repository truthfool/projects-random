package com.example.consistenthashing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Demo class that demonstrates consistent hashing functionality.
 * This class runs various scenarios to showcase the features of the consistent
 * hashing implementation.
 */
@Component
public class ConsistentHashingDemo implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(ConsistentHashingDemo.class);

    private final ConsistentHashingService service;

    public ConsistentHashingDemo(ConsistentHashingService service) {
        this.service = service;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting Consistent Hashing Demo...");

        // Wait a bit for the application to fully start
        Thread.sleep(2000);

        // Run various demo scenarios
        basicDemo();
        distributionDemo();
        nodeFailureDemo();
        concurrentRequestsDemo();
        performanceDemo();

        logger.info("Consistent Hashing Demo completed!");
    }

    /**
     * Demonstrates basic consistent hashing operations.
     */
    private void basicDemo() {
        logger.info("=== Basic Demo ===");

        // Test key lookups
        String[] testKeys = { "user-123", "product-456", "order-789", "session-abc", "cache-xyz" };

        for (String key : testKeys) {
            ConsistentHashRing.Node node = service.getNodeForKey(key);
            if (node != null) {
                logger.info("Key '{}' mapped to node: {}", key, node.getId());
            }
        }

        // Test getting multiple nodes for replication
        String replicationKey = "important-data";
        List<ConsistentHashRing.Node> replicas = service.getNodesForKey(replicationKey, 3);
        logger.info("Replicas for '{}': {}", replicationKey,
                replicas.stream().map(ConsistentHashRing.Node::getId).toList());
    }

    /**
     * Demonstrates the distribution of keys across nodes.
     */
    private void distributionDemo() {
        logger.info("=== Distribution Demo ===");

        // Generate many random keys and track distribution
        Map<String, Integer> distribution = new HashMap<>();
        Random random = new Random();

        for (int i = 0; i < 1000; i++) {
            String key = "key-" + random.nextInt(10000);
            ConsistentHashRing.Node node = service.getNodeForKey(key);
            if (node != null) {
                distribution.merge(node.getId(), 1, Integer::sum);
            }
        }

        // Show distribution
        logger.info("Key distribution across nodes:");
        distribution.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> logger.info("  {}: {} keys", entry.getKey(), entry.getValue()));

        // Show statistics
        ConsistentHashRing.HashRingStats stats = service.getStats();
        logger.info("Hash ring stats: {}", stats);
    }

    /**
     * Demonstrates how the system handles node failures.
     */
    private void nodeFailureDemo() {
        logger.info("=== Node Failure Demo ===");

        // Test keys before failure
        String[] testKeys = { "user-123", "product-456", "order-789" };
        Map<String, ConsistentHashRing.Node> beforeFailure = new HashMap<>();

        for (String key : testKeys) {
            beforeFailure.put(key, service.getNodeForKey(key));
        }

        logger.info("Node assignments before failure:");
        beforeFailure.forEach((key, node) -> logger.info("  {} -> {}", key, node != null ? node.getId() : "null"));

        // Simulate a node failure
        String failedNodeId = "node-3";
        logger.info("Simulating failure of node: {}", failedNodeId);
        boolean failureSimulated = service.simulateNodeFailure(failedNodeId);

        if (failureSimulated) {
            // Test keys after failure
            logger.info("Node assignments after failure:");
            for (String key : testKeys) {
                ConsistentHashRing.Node node = service.getNodeForKey(key);
                ConsistentHashRing.Node previousNode = beforeFailure.get(key);

                if (node != null && previousNode != null) {
                    if (node.getId().equals(previousNode.getId())) {
                        logger.info("  {} -> {} (unchanged)", key, node.getId());
                    } else {
                        logger.info("  {} -> {} (migrated from {})", key, node.getId(), previousNode.getId());
                    }
                }
            }
        }

        // Show updated statistics
        ConsistentHashRing.HashRingStats stats = service.getStats();
        logger.info("Updated hash ring stats: {}", stats);
    }

    /**
     * Demonstrates concurrent request handling.
     */
    private void concurrentRequestsDemo() {
        logger.info("=== Concurrent Requests Demo ===");

        int numThreads = 10;
        int requestsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                Random random = new Random();
                for (int j = 0; j < requestsPerThread; j++) {
                    String key = "thread-" + threadId + "-key-" + j;
                    ConsistentHashRing.Node node = service.getNodeForKey(key);
                    if (node != null) {
                        logger.debug("Thread {}: Key '{}' -> Node '{}'", threadId, key, node.getId());
                    }
                }
            }, executor);
            futures.add(future);
        }

        // Wait for all threads to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        logger.info("Completed {} concurrent requests across {} threads",
                numThreads * requestsPerThread, numThreads);
        logger.info("Total requests processed: {}", service.getTotalRequests());
    }

    /**
     * Demonstrates performance characteristics.
     */
    private void performanceDemo() {
        logger.info("=== Performance Demo ===");

        int numKeys = 1000; // Reduced from 10000 to avoid overwhelming the thread pool
        Random random = new Random();

        // Generate test keys
        List<String> testKeys = new ArrayList<>();
        for (int i = 0; i < numKeys; i++) {
            testKeys.add("perf-key-" + random.nextInt(100000));
        }

        // Measure lookup performance
        long startTime = System.currentTimeMillis();

        for (String key : testKeys) {
            service.getNodeForKey(key);
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double avgTime = (double) totalTime / numKeys;

        logger.info("Performance results:");
        logger.info("  Total keys processed: {}", numKeys);
        logger.info("  Total time: {} ms", totalTime);
        logger.info("  Average time per lookup: {:.3f} ms", avgTime);
        logger.info("  Lookups per second: {:.0f}", 1000.0 / avgTime);

        // Test async performance with batching to avoid overwhelming the thread pool
        logger.info("Testing async performance...");
        startTime = System.currentTimeMillis();

        int batchSize = 100; // Process in batches
        List<CompletableFuture<ConsistentHashRing.Node>> allFutures = new ArrayList<>();

        for (int i = 0; i < testKeys.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, testKeys.size());
            List<String> batch = testKeys.subList(i, endIndex);

            List<CompletableFuture<ConsistentHashRing.Node>> batchFutures = new ArrayList<>();
            for (String key : batch) {
                batchFutures.add(service.getNodeForKeyAsync(key));
            }

            // Wait for this batch to complete before starting the next
            CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0])).join();
            allFutures.addAll(batchFutures);
        }

        endTime = System.currentTimeMillis();
        totalTime = endTime - startTime;
        avgTime = (double) totalTime / numKeys;

        logger.info("Async performance results:");
        logger.info("  Total time: {} ms", totalTime);
        logger.info("  Average time per async lookup: {:.3f} ms", avgTime);
        logger.info("  Async lookups per second: {:.0f}", 1000.0 / avgTime);
    }

    /**
     * Demonstrates cache operations.
     */
    private void cacheDemo() {
        logger.info("=== Cache Demo ===");

        // Store some values in cache
        service.putInCache("user:123", "John Doe");
        service.putInCache("product:456", "Laptop");
        service.putInCache("order:789", "Pending");

        // Retrieve values from cache
        Object user = service.getFromCache("user:123");
        Object product = service.getFromCache("product:456");
        Object order = service.getFromCache("order:789");

        logger.info("Cached values:");
        logger.info("  user:123 -> {}", user);
        logger.info("  product:456 -> {}", product);
        logger.info("  order:789 -> {}", order);

        // Show cache statistics
        Map<String, Object> cacheStats = service.getCacheStats();
        logger.info("Cache stats: {}", cacheStats);
    }
}