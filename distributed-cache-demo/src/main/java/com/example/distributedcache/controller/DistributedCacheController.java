package com.example.distributedcache.controller;

import com.example.distributedcache.service.DistributedCacheService;
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
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * REST API controller for distributed cache operations.
 * Provides endpoints for cache management, data operations, and system
 * monitoring.
 */
@RestController
@RequestMapping("/api/v1/cache")
@Validated
@CrossOrigin(origins = "*")
public class DistributedCacheController {

    private static final Logger logger = LoggerFactory.getLogger(DistributedCacheController.class);

    private final DistributedCacheService cacheService;

    // Request and Response DTOs
    public record CachePutRequest(@NotBlank String key, @NotNull Object value,
            @Positive Integer ttlSeconds) {
    }

    public record CacheGetResponse(String key, Object value, boolean found, long timestamp) {
    }

    public record BatchPutRequest(Map<String, Object> keyValuePairs,
            @Positive Integer ttlSeconds) {
    }

    public record BatchGetRequest(List<String> keys) {
    }

    public record CacheStatsResponse(Map<String, Object> statistics,
            Map<String, Object> clusterInfo,
            long timestamp) {
    }

    public record HealthResponse(String status, String message, long timestamp) {
    }

    public DistributedCacheController(DistributedCacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * Stores a value in the distributed cache.
     */
    @PostMapping("/put")
    public ResponseEntity<ApiResponse<Boolean>> put(@Valid @RequestBody CachePutRequest request) {
        try {
            logger.info("Storing key: {} in distributed cache", request.key());

            Duration ttl = request.ttlSeconds() != null ? Duration.ofSeconds(request.ttlSeconds())
                    : Duration.ofHours(1);

            boolean success = cacheService.put(request.key(), request.value(), ttl);

            if (success) {
                return ResponseEntity.ok(ApiResponse.success(success, "Value stored successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to store value", false));
            }
        } catch (Exception e) {
            logger.error("Error storing key: {}", request.key(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error storing value: " + e.getMessage(), false));
        }
    }

    /**
     * Retrieves a value from the distributed cache.
     */
    @GetMapping("/get/{key}")
    public ResponseEntity<ApiResponse<CacheGetResponse>> get(@PathVariable @NotBlank String key) {
        try {
            logger.debug("Retrieving key: {} from distributed cache", key);

            Optional<Object> value = cacheService.get(key);

            CacheGetResponse response = new CacheGetResponse(
                    key,
                    value.orElse(null),
                    value.isPresent(),
                    System.currentTimeMillis());

            if (value.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(response, "Value retrieved successfully"));
            } else {
                return ResponseEntity.ok(ApiResponse.success(response, "Key not found"));
            }
        } catch (Exception e) {
            logger.error("Error retrieving key: {}", key, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving value: " + e.getMessage(), null));
        }
    }

    /**
     * Asynchronously retrieves a value from the distributed cache.
     */
    @GetMapping("/get/{key}/async")
    public CompletableFuture<ResponseEntity<ApiResponse<CacheGetResponse>>> getAsync(
            @PathVariable @NotBlank String key) {
        return cacheService.getAsync(key)
                .thenApply(value -> {
                    CacheGetResponse response = new CacheGetResponse(
                            key,
                            value.orElse(null),
                            value.isPresent(),
                            System.currentTimeMillis());

                    if (value.isPresent()) {
                        return ResponseEntity.ok(ApiResponse.success(response, "Value retrieved asynchronously"));
                    } else {
                        return ResponseEntity.ok(ApiResponse.success(response, "Key not found"));
                    }
                })
                .exceptionally(throwable -> {
                    logger.error("Error in async retrieval for key: {}", key, throwable);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("Error retrieving value: " + throwable.getMessage(), null));
                });
    }

    /**
     * Removes a value from the distributed cache.
     */
    @DeleteMapping("/remove/{key}")
    public ResponseEntity<ApiResponse<Boolean>> remove(@PathVariable @NotBlank String key) {
        try {
            logger.info("Removing key: {} from distributed cache", key);

            boolean removed = cacheService.remove(key);

            if (removed) {
                return ResponseEntity.ok(ApiResponse.success(true, "Value removed successfully"));
            } else {
                return ResponseEntity.ok(ApiResponse.success(false, "Key not found"));
            }
        } catch (Exception e) {
            logger.error("Error removing key: {}", key, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error removing value: " + e.getMessage(), false));
        }
    }

    /**
     * Checks if a key exists in the distributed cache.
     */
    @GetMapping("/contains/{key}")
    public ResponseEntity<ApiResponse<Boolean>> containsKey(@PathVariable @NotBlank String key) {
        try {
            logger.debug("Checking existence of key: {}", key);

            boolean exists = cacheService.containsKey(key);

            return ResponseEntity.ok(ApiResponse.success(exists,
                    exists ? "Key exists" : "Key does not exist"));
        } catch (Exception e) {
            logger.error("Error checking key existence: {}", key, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error checking key existence: " + e.getMessage(), false));
        }
    }

    /**
     * Stores multiple values in batch.
     */
    @PostMapping("/batch/put")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> putMultiple(
            @Valid @RequestBody BatchPutRequest request) {
        try {
            logger.info("Storing {} keys in batch", request.keyValuePairs().size());

            Duration ttl = request.ttlSeconds() != null ? Duration.ofSeconds(request.ttlSeconds())
                    : Duration.ofHours(1);

            Map<String, Boolean> results = cacheService.putMultiple(request.keyValuePairs(), ttl);

            return ResponseEntity.ok(ApiResponse.success(results, "Batch operation completed"));
        } catch (Exception e) {
            logger.error("Error in batch put operation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error in batch operation: " + e.getMessage(), null));
        }
    }

    /**
     * Retrieves multiple values in batch.
     */
    @PostMapping("/batch/get")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMultiple(
            @Valid @RequestBody BatchGetRequest request) {
        try {
            logger.info("Retrieving {} keys in batch", request.keys().size());

            Map<String, Object> results = cacheService.getMultiple(request.keys());

            return ResponseEntity.ok(ApiResponse.success(results, "Batch retrieval completed"));
        } catch (Exception e) {
            logger.error("Error in batch get operation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error in batch operation: " + e.getMessage(), null));
        }
    }

    /**
     * Clears all data from the distributed cache.
     */
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Boolean>> clear() {
        try {
            logger.info("Clearing all data from distributed cache");

            boolean cleared = cacheService.clear();

            if (cleared) {
                return ResponseEntity.ok(ApiResponse.success(true, "Cache cleared successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to clear cache", false));
            }
        } catch (Exception e) {
            logger.error("Error clearing cache", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error clearing cache: " + e.getMessage(), false));
        }
    }

    /**
     * Gets cache statistics and cluster information.
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<CacheStatsResponse>> getStats() {
        try {
            logger.debug("Retrieving cache statistics");

            Map<String, Object> statistics = cacheService.getStatistics();
            Map<String, Object> clusterInfo = cacheService.getClusterInfo();

            CacheStatsResponse response = new CacheStatsResponse(
                    statistics,
                    clusterInfo,
                    System.currentTimeMillis());

            return ResponseEntity.ok(ApiResponse.success(response, "Statistics retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error retrieving statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving statistics: " + e.getMessage(), null));
        }
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<HealthResponse>> health() {
        try {
            Map<String, Object> clusterInfo = cacheService.getClusterInfo();
            int memberCount = (Integer) clusterInfo.get("memberCount");

            String status = memberCount > 0 ? "HEALTHY" : "UNHEALTHY";
            String message = memberCount > 0 ? "Distributed cache is operational" : "No cluster members available";

            HealthResponse response = new HealthResponse(status, message, System.currentTimeMillis());

            return ResponseEntity.ok(ApiResponse.success(response, "Health check completed"));
        } catch (Exception e) {
            logger.error("Error in health check", e);
            HealthResponse response = new HealthResponse("ERROR", "Health check failed", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Health check failed: " + e.getMessage(), response));
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

        public ApiResponse(boolean success, String message, T data) {
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

        // Getters
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