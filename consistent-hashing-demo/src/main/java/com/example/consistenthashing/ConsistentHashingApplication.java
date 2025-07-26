package com.example.consistenthashing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main Spring Boot application for the Consistent Hashing Demo.
 * This application demonstrates scalable consistent hashing with concurrent
 * request handling.
 */
@SpringBootApplication
@EnableAsync
public class ConsistentHashingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsistentHashingApplication.class, args);
    }
}