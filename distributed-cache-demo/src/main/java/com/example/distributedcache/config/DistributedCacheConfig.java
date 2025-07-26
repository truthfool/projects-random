package com.example.distributedcache.config;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration class for distributed cache setup.
 * Configures Hazelcast for in-memory distributed caching and Redis for
 * persistence.
 */
@Configuration
public class DistributedCacheConfig {

    private static final Logger logger = LoggerFactory.getLogger(DistributedCacheConfig.class);

    @Value("${hazelcast.cluster.name:distributed-cache-cluster}")
    private String clusterName;

    @Value("${hazelcast.port:5701}")
    private int hazelcastPort;

    @Value("${hazelcast.backup.count:1}")
    private int backupCount;

    @Value("${hazelcast.max.size:10000}")
    private int maxSize;

    @Value("${hazelcast.ttl.seconds:3600}")
    private int ttlSeconds;

    /**
     * Creates and configures Hazelcast instance for distributed caching.
     */
    @Bean
    public HazelcastInstance hazelcastInstance() {
        logger.info("Initializing Hazelcast instance on port: {}", hazelcastPort);

        Config config = new Config();
        config.setClusterName(clusterName);
        config.setInstanceName("distributed-cache-instance");

        // Network configuration
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPort(hazelcastPort);
        networkConfig.setPortAutoIncrement(true);

        // Join configuration for cluster discovery
        JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig().setEnabled(true);
        joinConfig.getTcpIpConfig().addMember("127.0.0.1");

        // Map configuration for cache
        MapConfig mapConfig = new MapConfig("distributed-cache");
        mapConfig.setBackupCount(backupCount);
        mapConfig.setTimeToLiveSeconds(ttlSeconds);
        mapConfig.setEvictionConfig(new EvictionConfig()
                .setEvictionPolicy(EvictionPolicy.LRU)
                .setMaxSizePolicy(MaxSizePolicy.PER_NODE)
                .setSize(maxSize));

        // Near cache configuration for better performance
        NearCacheConfig nearCacheConfig = new NearCacheConfig();
        nearCacheConfig.setTimeToLiveSeconds(ttlSeconds);
        mapConfig.setNearCacheConfig(nearCacheConfig);

        config.addMapConfig(mapConfig);

        return Hazelcast.newHazelcastInstance(config);
    }

    /**
     * Configures Redis template for persistent caching.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        logger.info("Configuring Redis template");

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for values
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Configures async task executor for concurrent operations.
     */
    @Bean("cacheTaskExecutor")
    public Executor cacheTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("cache-async-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}