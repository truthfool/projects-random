package com.example.consistenthashing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service layer for consistent hashing operations.
 * Provides business logic, caching, and async processing capabilities.
 */
@Service
public class ConsistentHashingService {
    private static final Logger logger = LoggerFactory.getLogger(ConsistentHashingService.class);

    private final ConsistentHashRing hashRing;
    private final Map<String, Object> cache;
    private final AtomicLong requestCounter;

    public ConsistentHashingService() {
        this.hashRing = new ConsistentHashRing();
        this.cache = new ConcurrentHashMap<>();
        this.requestCounter = new AtomicLong(0);

        // Initialize with some default nodes
        initializeDefaultNodes();
    }

    /**
     * Initializes the hash ring with some default nodes for demonstration.
     */
    private void initializeDefaultNodes() {
        addNode("node-1", "192.168.1.10:8080");
        addNode("node-2", "192.168.1.11:8080");
        addNode("node-3", "192.168.1.12:8080");
        addNode("node-4", "192.168.1.13:8080");
        addNode("node-5", "192.168.1.14:8080");

        logger.info("Initialized hash ring with {} default nodes", hashRing.getNodeCount());
    }

    /**
     * Adds a new node to the hash ring.
     * 
     * @param nodeId      Unique identifier for the node
     * @param nodeAddress Network address of the node
     * @return true if the node was added successfully
     */
    public boolean addNode(String nodeId, String nodeAddress) {
        boolean success = hashRing.addNode(nodeId, nodeAddress);
        if (success) {
            logger.info("Successfully added node: {} at {}", nodeId, nodeAddress);
        }
        return success;
    }

    /**
     * Removes a node from the hash ring.
     * 
     * @param nodeId Unique identifier of the node to remove
     * @return true if the node was removed successfully
     */
    public boolean removeNode(String nodeId) {
        boolean success = hashRing.removeNode(nodeId);
        if (success) {
            logger.info("Successfully removed node: {}", nodeId);
        }
        return success;
    }

    /**
     * Gets the node responsible for a given key.
     * 
     * @param key The key to hash and find the responsible node for
     * @return The physical node responsible for the key
     */
    public ConsistentHashRing.Node getNodeForKey(String key) {
        long requestId = requestCounter.incrementAndGet();
        logger.debug("Request {}: Getting node for key: {}", requestId, key);

        ConsistentHashRing.Node node = hashRing.getNode(key);
        if (node != null) {
            logger.debug("Request {}: Key '{}' mapped to node: {}", requestId, key, node.getId());
        } else {
            logger.warn("Request {}: No node found for key: {}", requestId, key);
        }

        return node;
    }

    /**
     * Gets multiple nodes responsible for a given key (for replication).
     * 
     * @param key   The key to hash and find responsible nodes for
     * @param count Number of nodes to return
     * @return List of physical nodes responsible for the key
     */
    public List<ConsistentHashRing.Node> getNodesForKey(String key, int count) {
        long requestId = requestCounter.incrementAndGet();
        logger.debug("Request {}: Getting {} nodes for key: {}", requestId, count, key);

        List<ConsistentHashRing.Node> nodes = hashRing.getNodes(key, count);
        logger.debug("Request {}: Key '{}' mapped to {} nodes: {}",
                requestId, key, nodes.size(),
                nodes.stream().map(ConsistentHashRing.Node::getId).toList());

        return nodes;
    }

    /**
     * Stores a value in the cache with the given key.
     * 
     * @param key   The cache key
     * @param value The value to store
     */
    public void putInCache(String key, Object value) {
        cache.put(key, value);
        logger.debug("Stored value in cache for key: {}", key);
    }

    /**
     * Retrieves a value from the cache.
     * 
     * @param key The cache key
     * @return The cached value, or null if not found
     */
    public Object getFromCache(String key) {
        Object value = cache.get(key);
        if (value != null) {
            logger.debug("Cache hit for key: {}", key);
        } else {
            logger.debug("Cache miss for key: {}", key);
        }
        return value;
    }

    /**
     * Removes a value from the cache.
     * 
     * @param key The cache key to remove
     * @return The removed value, or null if not found
     */
    public Object removeFromCache(String key) {
        Object value = cache.remove(key);
        if (value != null) {
            logger.debug("Removed value from cache for key: {}", key);
        }
        return value;
    }

    /**
     * Gets statistics about the hash ring.
     * 
     * @return HashRingStats containing distribution information
     */
    public ConsistentHashRing.HashRingStats getStats() {
        return hashRing.getStats();
    }

    /**
     * Gets all nodes in the hash ring.
     * 
     * @return Set of all physical nodes
     */
    public List<ConsistentHashRing.Node> getAllNodes() {
        return hashRing.getAllNodes().stream().toList();
    }

    /**
     * Gets cache statistics.
     * 
     * @return Map containing cache statistics
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("size", cache.size());
        stats.put("totalRequests", requestCounter.get());
        return stats;
    }

    /**
     * Clears the cache.
     */
    public void clearCache() {
        int size = cache.size();
        cache.clear();
        logger.info("Cleared cache with {} entries", size);
    }

    /**
     * Asynchronously processes a key lookup operation.
     * 
     * @param key The key to process
     * @return CompletableFuture containing the responsible node
     */
    @Async
    public CompletableFuture<ConsistentHashRing.Node> getNodeForKeyAsync(String key) {
        return CompletableFuture.completedFuture(getNodeForKey(key));
    }

    /**
     * Asynchronously processes multiple key lookups.
     * 
     * @param keys List of keys to process
     * @return CompletableFuture containing a map of key to node mappings
     */
    @Async
    public CompletableFuture<Map<String, ConsistentHashRing.Node>> getNodesForKeysAsync(List<String> keys) {
        Map<String, ConsistentHashRing.Node> result = new ConcurrentHashMap<>();

        keys.parallelStream().forEach(key -> {
            ConsistentHashRing.Node node = getNodeForKey(key);
            if (node != null) {
                result.put(key, node);
            }
        });

        return CompletableFuture.completedFuture(result);
    }

    /**
     * Performs a bulk operation to add multiple nodes.
     * 
     * @param nodes Map of node ID to node address
     * @return Map of node ID to success status
     */
    public Map<String, Boolean> addNodesBulk(Map<String, String> nodes) {
        Map<String, Boolean> results = new ConcurrentHashMap<>();

        nodes.entrySet().parallelStream().forEach(entry -> {
            String nodeId = entry.getKey();
            String nodeAddress = entry.getValue();
            boolean success = addNode(nodeId, nodeAddress);
            results.put(nodeId, success);
        });

        logger.info("Bulk node addition completed. Results: {}", results);
        return results;
    }

    /**
     * Performs a bulk operation to remove multiple nodes.
     * 
     * @param nodeIds List of node IDs to remove
     * @return Map of node ID to success status
     */
    public Map<String, Boolean> removeNodesBulk(List<String> nodeIds) {
        Map<String, Boolean> results = new ConcurrentHashMap<>();

        nodeIds.parallelStream().forEach(nodeId -> {
            boolean success = removeNode(nodeId);
            results.put(nodeId, success);
        });

        logger.info("Bulk node removal completed. Results: {}", results);
        return results;
    }

    /**
     * Simulates a node failure by removing it from the hash ring.
     * 
     * @param nodeId The ID of the failed node
     * @return true if the node was successfully removed
     */
    public boolean simulateNodeFailure(String nodeId) {
        logger.warn("Simulating failure for node: {}", nodeId);
        return removeNode(nodeId);
    }

    /**
     * Gets the total number of requests processed.
     * 
     * @return Total request count
     */
    public long getTotalRequests() {
        return requestCounter.get();
    }

    /**
     * Resets the request counter.
     */
    public void resetRequestCounter() {
        requestCounter.set(0);
        logger.info("Request counter reset");
    }
}