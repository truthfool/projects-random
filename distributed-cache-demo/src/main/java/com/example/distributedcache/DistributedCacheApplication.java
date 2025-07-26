package com.example.distributedcache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application for the Distributed Cache Demo.
 * This application demonstrates a scalable distributed caching system with high
 * availability
 * and concurrent request handling capabilities.
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class DistributedCacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributedCacheApplication.class, args);
    }
}