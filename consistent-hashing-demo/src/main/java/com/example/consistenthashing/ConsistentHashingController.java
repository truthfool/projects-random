package com.example.consistenthashing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST API controller for consistent hashing operations.
 * Provides endpoints for node management, key lookups, and system statistics.
 */
@RestController
@RequestMapping("/api/v1/consistent-hashing")
@Validated
@CrossOrigin(origins = "*")
public class ConsistentHashingController {
    private static final Logger logger = LoggerFactory.getLogger(ConsistentHashingController.class);

    private final ConsistentHashingService service;

    // Request and Response DTOs
    public record AddNodeRequest(@NotBlank String nodeId, @NotBlank String nodeAddress) {
    }

    public record NodeResponse(String id, String address, long createdAt) {
    }

    public record StatsResponse(int physicalNodeCount, int virtualNodeCount,
            int virtualNodesPerPhysicalNode, Map<String, Integer> nodeDistribution) {
    }

    public record CacheRequest(@NotBlank String key, @NotNull Object value) {
    }

    public record HealthResponse(String status, long totalRequests, int nodeCount, long timestamp) {
    }

    public ConsistentHashingController(ConsistentHashingService service) {
        this.service = service;
    }

    /**
     * Adds a new node to the hash ring.
     * 
     * @param request Node addition request
     * @return Response indicating success or failure
     */
    @PostMapping("/nodes")
    public ResponseEntity<ApiResponse<Boolean>> addNode(@Valid @RequestBody AddNodeRequest request) {
        try {
            logger.info("Adding node: {} at {}", request.nodeId(), request.nodeAddress());
            boolean success = service.addNode(request.nodeId(), request.nodeAddress());

            if (success) {
                return ResponseEntity.ok(ApiResponse.success(success, "Node added successfully"));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Node already exists", false));
            }
        } catch (Exception e) {
            logger.error("Error adding node: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to add node: " + e.getMessage(), false));
        }
    }

    /**
     * Removes a node from the hash ring.
     * 
     * @param nodeId ID of the node to remove
     * @return Response indicating success or failure
     */
    @DeleteMapping("/nodes/{nodeId}")
    public ResponseEntity<ApiResponse<Boolean>> removeNode(@PathVariable @NotBlank String nodeId) {
        try {
            logger.info("Removing node: {}", nodeId);
            boolean success = service.removeNode(nodeId);

            if (success) {
                return ResponseEntity.ok(ApiResponse.success(success, "Node removed successfully"));
            } else {
                return ResponseEntity.notFound()
                        .build();
            }
        } catch (Exception e) {
            logger.error("Error removing node: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to remove node: " + e.getMessage(), false));
        }
    }

    /**
     * Gets the node responsible for a given key.
     * 
     * @param key The key to lookup
     * @return The responsible node
     */
    @GetMapping("/lookup/{key}")
    public ResponseEntity<ApiResponse<NodeResponse>> getNodeForKey(@PathVariable @NotBlank String key) {
        try {
            logger.debug("Looking up node for key: {}", key);
            ConsistentHashRing.Node node = service.getNodeForKey(key);

            if (node != null) {
                NodeResponse response = new NodeResponse(
                        node.getId(),
                        node.getAddress(),
                        node.getCreatedAt());
                return ResponseEntity.ok(ApiResponse.success(response, "Node found"));
            } else {
                return ResponseEntity.notFound()
                        .build();
            }
        } catch (Exception e) {
            logger.error("Error looking up node for key {}: {}", key, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to lookup node: " + e.getMessage(), null));
        }
    }

    /**
     * Gets multiple nodes responsible for a given key (for replication).
     * 
     * @param key   The key to lookup
     * @param count Number of nodes to return (default: 3)
     * @return List of responsible nodes
     */
    @GetMapping("/lookup/{key}/replicas")
    public ResponseEntity<ApiResponse<List<NodeResponse>>> getNodesForKey(
            @PathVariable @NotBlank String key,
            @RequestParam(defaultValue = "3") @Positive int count) {
        try {
            logger.debug("Looking up {} nodes for key: {}", count, key);
            List<ConsistentHashRing.Node> nodes = service.getNodesForKey(key, count);

            List<NodeResponse> response = nodes.stream()
                    .map(node -> new NodeResponse(node.getId(), node.getAddress(), node.getCreatedAt()))
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(response, "Nodes found"));
        } catch (Exception e) {
            logger.error("Error looking up nodes for key {}: {}", key, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to lookup nodes: " + e.getMessage(), null));
        }
    }

    /**
     * Asynchronously gets the node responsible for a given key.
     * 
     * @param key The key to lookup
     * @return CompletableFuture containing the responsible node
     */
    @GetMapping("/lookup/{key}/async")
    public CompletableFuture<ResponseEntity<ApiResponse<NodeResponse>>> getNodeForKeyAsync(
            @PathVariable @NotBlank String key) {
        return service.getNodeForKeyAsync(key)
                .thenApply(node -> {
                    if (node != null) {
                        NodeResponse response = new NodeResponse(
                                node.getId(),
                                node.getAddress(),
                                node.getCreatedAt());
                        return ResponseEntity.ok(ApiResponse.success(response, "Node found"));
                    } else {
                        return ResponseEntity.notFound().<ApiResponse<NodeResponse>>build();
                    }
                })
                .exceptionally(throwable -> {
                    logger.error("Error in async lookup for key {}: {}", key, throwable.getMessage(), throwable);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("Failed to lookup node: " + throwable.getMessage(), null));
                });
    }

    /**
     * Gets all nodes in the hash ring.
     * 
     * @return List of all nodes
     */
    @GetMapping("/nodes")
    public ResponseEntity<ApiResponse<List<NodeResponse>>> getAllNodes() {
        try {
            List<ConsistentHashRing.Node> nodes = service.getAllNodes();
            List<NodeResponse> response = nodes.stream()
                    .map(node -> new NodeResponse(node.getId(), node.getAddress(), node.getCreatedAt()))
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(response, "Nodes retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error getting all nodes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get nodes: " + e.getMessage(), null));
        }
    }

    /**
     * Gets statistics about the hash ring.
     * 
     * @return Hash ring statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<StatsResponse>> getStats() {
        try {
            ConsistentHashRing.HashRingStats stats = service.getStats();
            StatsResponse response = new StatsResponse(
                    stats.getPhysicalNodeCount(),
                    stats.getVirtualNodeCount(),
                    stats.getVirtualNodesPerPhysicalNode(),
                    stats.getNodeDistribution());

            return ResponseEntity.ok(ApiResponse.success(response, "Statistics retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error getting stats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get stats: " + e.getMessage(), null));
        }
    }

    /**
     * Gets cache statistics.
     * 
     * @return Cache statistics
     */
    @GetMapping("/cache/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCacheStats() {
        try {
            Map<String, Object> stats = service.getCacheStats();
            return ResponseEntity.ok(ApiResponse.success(stats, "Cache statistics retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error getting cache stats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get cache stats: " + e.getMessage(), null));
        }
    }

    /**
     * Stores a value in the cache.
     * 
     * @param request Cache storage request
     * @return Response indicating success
     */
    @PostMapping("/cache")
    public ResponseEntity<ApiResponse<String>> putInCache(@Valid @RequestBody CacheRequest request) {
        try {
            service.putInCache(request.key(), request.value());
            return ResponseEntity.ok(ApiResponse.success("Value stored", "Value stored successfully"));
        } catch (Exception e) {
            logger.error("Error storing value in cache: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to store value: " + e.getMessage(), null));
        }
    }

    /**
     * Retrieves a value from the cache.
     * 
     * @param key The cache key
     * @return The cached value
     */
    @GetMapping("/cache/{key}")
    public ResponseEntity<ApiResponse<Object>> getFromCache(@PathVariable @NotBlank String key) {
        try {
            Object value = service.getFromCache(key);
            if (value != null) {
                return ResponseEntity.ok(ApiResponse.success(value, "Value retrieved successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving value from cache: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve value: " + e.getMessage(), null));
        }
    }

    /**
     * Clears the cache.
     * 
     * @return Response indicating success
     */
    @DeleteMapping("/cache")
    public ResponseEntity<ApiResponse<String>> clearCache() {
        try {
            service.clearCache();
            return ResponseEntity.ok(ApiResponse.success("Cache cleared", "Cache cleared successfully"));
        } catch (Exception e) {
            logger.error("Error clearing cache: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to clear cache: " + e.getMessage(), null));
        }
    }

    /**
     * Simulates a node failure.
     * 
     * @param nodeId ID of the node to simulate failure for
     * @return Response indicating success or failure
     */
    @PostMapping("/nodes/{nodeId}/fail")
    public ResponseEntity<ApiResponse<Boolean>> simulateNodeFailure(@PathVariable @NotBlank String nodeId) {
        try {
            logger.warn("Simulating failure for node: {}", nodeId);
            boolean success = service.simulateNodeFailure(nodeId);

            if (success) {
                return ResponseEntity.ok(ApiResponse.success(success, "Node failure simulated successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error simulating node failure: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to simulate node failure: " + e.getMessage(), false));
        }
    }

    /**
     * Gets system health information.
     * 
     * @return Health information
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<HealthResponse>> getHealth() {
        try {
            long totalRequests = service.getTotalRequests();
            int nodeCount = service.getAllNodes().size();

            HealthResponse health = new HealthResponse(
                    "UP",
                    totalRequests,
                    nodeCount,
                    System.currentTimeMillis());

            return ResponseEntity.ok(ApiResponse.success(health, "System is healthy"));
        } catch (Exception e) {
            logger.error("Error getting health: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get health: " + e.getMessage(), null));
        }
    }

    /**
     * Generic API response wrapper.
     */
    public static class ApiResponse<T> {
        private final boolean success;
        private final String message;
        private final T data;
        private final long timestamp;

        private ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        public static <T> ApiResponse<T> success(T data, String message) {
            return new ApiResponse<>(true, message, data);
        }

        public static <T> ApiResponse<T> error(String message, T data) {
            return new ApiResponse<>(false, message, data);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public T getData() {
            return data;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}