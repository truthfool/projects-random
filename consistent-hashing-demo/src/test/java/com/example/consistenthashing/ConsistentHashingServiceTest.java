package com.example.consistenthashing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the ConsistentHashingService.
 */
class ConsistentHashingServiceTest {

    private ConsistentHashingService service;

    @BeforeEach
    void setUp() {
        service = new ConsistentHashingService();
    }

    @Test
    @DisplayName("Should add nodes successfully")
    void testAddNode() {
        // Given
        String nodeId = "test-node-1";
        String nodeAddress = "192.168.1.100:8080";

        // When
        boolean result = service.addNode(nodeId, nodeAddress);

        // Then
        assertTrue(result);
        List<ConsistentHashRing.Node> nodes = service.getAllNodes();
        assertTrue(nodes.stream().anyMatch(node -> node.getId().equals(nodeId)));
    }

    @Test
    @DisplayName("Should not add duplicate nodes")
    void testAddDuplicateNode() {
        // Given
        String nodeId = "test-node-2";
        String nodeAddress = "192.168.1.101:8080";

        // When
        boolean firstResult = service.addNode(nodeId, nodeAddress);
        boolean secondResult = service.addNode(nodeId, nodeAddress);

        // Then
        assertTrue(firstResult);
        assertFalse(secondResult);
    }

    @Test
    @DisplayName("Should remove nodes successfully")
    void testRemoveNode() {
        // Given
        String nodeId = "test-node-3";
        String nodeAddress = "192.168.1.102:8080";
        service.addNode(nodeId, nodeAddress);

        // When
        boolean result = service.removeNode(nodeId);

        // Then
        assertTrue(result);
        List<ConsistentHashRing.Node> nodes = service.getAllNodes();
        assertFalse(nodes.stream().anyMatch(node -> node.getId().equals(nodeId)));
    }

    @Test
    @DisplayName("Should return null for non-existent node removal")
    void testRemoveNonExistentNode() {
        // Given
        String nodeId = "non-existent-node";

        // When
        boolean result = service.removeNode(nodeId);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should consistently map keys to nodes")
    void testConsistentKeyMapping() {
        // Given
        String key = "test-key-123";

        // When
        ConsistentHashRing.Node node1 = service.getNodeForKey(key);
        ConsistentHashRing.Node node2 = service.getNodeForKey(key);

        // Then
        assertNotNull(node1);
        assertNotNull(node2);
        assertEquals(node1.getId(), node2.getId());
    }

    @Test
    @DisplayName("Should return multiple nodes for replication")
    void testGetMultipleNodes() {
        // Given
        String key = "replication-key";
        int replicaCount = 3;

        // When
        List<ConsistentHashRing.Node> nodes = service.getNodesForKey(key, replicaCount);

        // Then
        assertNotNull(nodes);
        assertTrue(nodes.size() <= replicaCount);
        assertTrue(nodes.size() > 0);

        // All nodes should be unique
        List<String> nodeIds = nodes.stream()
                .map(ConsistentHashRing.Node::getId)
                .collect(Collectors.toList());
        assertEquals(nodeIds.size(), nodeIds.stream().distinct().count());
    }

    @Test
    @DisplayName("Should handle cache operations correctly")
    void testCacheOperations() {
        // Given
        String key = "cache-key";
        String value = "cache-value";

        // When
        service.putInCache(key, value);
        Object retrievedValue = service.getFromCache(key);

        // Then
        assertEquals(value, retrievedValue);
    }

    @Test
    @DisplayName("Should return null for non-existent cache key")
    void testCacheMiss() {
        // Given
        String key = "non-existent-cache-key";

        // When
        Object value = service.getFromCache(key);

        // Then
        assertNull(value);
    }

    @Test
    @DisplayName("Should clear cache successfully")
    void testClearCache() {
        // Given
        service.putInCache("key1", "value1");
        service.putInCache("key2", "value2");

        // When
        service.clearCache();

        // Then
        assertNull(service.getFromCache("key1"));
        assertNull(service.getFromCache("key2"));
    }

    @Test
    @DisplayName("Should provide accurate statistics")
    void testGetStats() {
        // Given
        service.addNode("stats-node-1", "192.168.1.200:8080");
        service.addNode("stats-node-2", "192.168.1.201:8080");

        // When
        ConsistentHashRing.HashRingStats stats = service.getStats();

        // Then
        assertNotNull(stats);
        assertTrue(stats.getPhysicalNodeCount() >= 2);
        assertTrue(stats.getVirtualNodeCount() > 0);
        assertNotNull(stats.getNodeDistribution());
    }

    @Test
    @DisplayName("Should handle async operations correctly")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testAsyncOperations() throws Exception {
        // Given
        String key = "async-key";

        // When
        CompletableFuture<ConsistentHashRing.Node> future = service.getNodeForKeyAsync(key);
        ConsistentHashRing.Node node = future.get(5, TimeUnit.SECONDS);

        // Then
        assertNotNull(node);
    }

    @Test
    @DisplayName("Should simulate node failure correctly")
    void testSimulateNodeFailure() {
        // Given
        String nodeId = "failure-test-node";
        String nodeAddress = "192.168.1.202:8080";
        service.addNode(nodeId, nodeAddress);

        // When
        boolean result = service.simulateNodeFailure(nodeId);

        // Then
        assertTrue(result);
        List<ConsistentHashRing.Node> nodes = service.getAllNodes();
        assertFalse(nodes.stream().anyMatch(node -> node.getId().equals(nodeId)));
    }

    @Test
    @DisplayName("Should track request count correctly")
    void testRequestCount() {
        // Given
        long initialCount = service.getTotalRequests();

        // When
        service.getNodeForKey("key1");
        service.getNodeForKey("key2");
        service.getNodeForKey("key3");

        // Then
        long finalCount = service.getTotalRequests();
        assertEquals(initialCount + 3, finalCount);
    }

    @Test
    @DisplayName("Should reset request counter")
    void testResetRequestCounter() {
        // Given
        service.getNodeForKey("key1");
        service.getNodeForKey("key2");
        assertTrue(service.getTotalRequests() > 0);

        // When
        service.resetRequestCounter();

        // Then
        assertEquals(0, service.getTotalRequests());
    }

    @Test
    @DisplayName("Should provide cache statistics")
    void testGetCacheStats() {
        // Given
        service.putInCache("key1", "value1");
        service.putInCache("key2", "value2");

        // When
        Map<String, Object> cacheStats = service.getCacheStats();

        // Then
        assertNotNull(cacheStats);
        assertTrue(cacheStats.containsKey("size"));
        assertTrue(cacheStats.containsKey("totalRequests"));
        assertTrue((Integer) cacheStats.get("size") >= 2);
    }

    @RepeatedTest(10)
    @DisplayName("Should maintain consistent distribution across multiple runs")
    void testConsistentDistribution() {
        // Given
        String key = "distribution-test-key";

        // When
        ConsistentHashRing.Node node = service.getNodeForKey(key);

        // Then
        assertNotNull(node);
        assertNotNull(node.getId());
        assertNotNull(node.getAddress());
        assertTrue(node.getCreatedAt() > 0);
    }

    @Test
    @DisplayName("Should handle edge cases gracefully")
    void testEdgeCases() {
        // Test with empty key
        assertNull(service.getNodeForKey(""));

        // Test with null key
        assertNull(service.getNodeForKey(null));

        // Test with very long key
        String longKey = "a".repeat(10000);
        assertNotNull(service.getNodeForKey(longKey));

        // Test with special characters
        String specialKey = "key-with-special-chars!@#$%^&*()";
        assertNotNull(service.getNodeForKey(specialKey));
    }

    @Test
    @DisplayName("Should handle concurrent access safely")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testConcurrentAccess() throws Exception {
        // Given
        int numThreads = 10;
        int operationsPerThread = 100;

        // When
        List<CompletableFuture<Void>> futures = new java.util.ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    String key = "concurrent-key-" + threadId + "-" + j;
                    ConsistentHashRing.Node node = service.getNodeForKey(key);
                    assertNotNull(node);
                }
            });
            futures.add(future);
        }

        // Wait for all threads to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

        // Then
        long expectedRequests = (long) numThreads * operationsPerThread;
        assertTrue(service.getTotalRequests() >= expectedRequests);
    }

    @Test
    @DisplayName("Should handle node addition and removal stress test")
    void testStressTest() {
        // Given
        int numNodes = 50;
        int numKeys = 1000;

        // When - Add many nodes
        for (int i = 0; i < numNodes; i++) {
            String nodeId = "stress-node-" + i;
            String nodeAddress = "192.168.1." + (100 + i) + ":8080";
            assertTrue(service.addNode(nodeId, nodeAddress));
        }

        // Test key distribution
        Map<String, Integer> distribution = new java.util.HashMap<>();
        for (int i = 0; i < numKeys; i++) {
            String key = "stress-key-" + i;
            ConsistentHashRing.Node node = service.getNodeForKey(key);
            if (node != null) {
                distribution.merge(node.getId(), 1, Integer::sum);
            }
        }

        // Then
        assertTrue(distribution.size() > 0);

        // Remove some nodes
        for (int i = 0; i < 10; i++) {
            String nodeId = "stress-node-" + i;
            assertTrue(service.removeNode(nodeId));
        }

        // Test that remaining nodes can still handle requests
        for (int i = 0; i < 100; i++) {
            String key = "stress-key-after-removal-" + i;
            ConsistentHashRing.Node node = service.getNodeForKey(key);
            assertNotNull(node);
        }
    }
}