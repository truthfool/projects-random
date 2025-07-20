package com.example.loadbalancer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Demo class to showcase different load balancing algorithms.
 */
public class LoadBalancerDemo {

    public static void main(String[] args) {
        LoadBalancerDemo demo = new LoadBalancerDemo();

        // Demo all algorithms
        demo.demoRoundRobin();
        demo.demoWeightedRoundRobin();
        demo.demoLeastActiveConnections();
        demo.demoLeastResponseTime();
    }

    /**
     * Demonstrates Round Robin algorithm.
     */
    public void demoRoundRobin() {
        System.out.println("\n=== Round Robin Algorithm Demo ===");

        List<Server> servers = createTestServers();
        LoadBalancer loadBalancer = LoadBalancerFactory.createLoadBalancer(
                LoadBalancerFactory.AlgorithmType.ROUND_ROBIN, servers);

        // Process multiple requests
        for (int i = 1; i <= 10; i++) {
            String response = loadBalancer.processRequest("REQ-" + i);
            System.out.println("Request " + i + ": " + response);
        }

        System.out.println("\nStatistics:\n" + loadBalancer.getStatistics());
    }

    /**
     * Demonstrates Weighted Round Robin algorithm.
     */
    public void demoWeightedRoundRobin() {
        System.out.println("\n=== Weighted Round Robin Algorithm Demo ===");

        List<Server> servers = createWeightedTestServers();
        LoadBalancer loadBalancer = LoadBalancerFactory.createLoadBalancer(
                LoadBalancerFactory.AlgorithmType.WEIGHTED_ROUND_ROBIN, servers);

        // Process multiple requests
        for (int i = 1; i <= 15; i++) {
            String response = loadBalancer.processRequest("REQ-" + i);
            System.out.println("Request " + i + ": " + response);
        }

        System.out.println("\nStatistics:\n" + loadBalancer.getStatistics());
    }

    /**
     * Demonstrates Least Active Connections algorithm.
     */
    public void demoLeastActiveConnections() {
        System.out.println("\n=== Least Active Connections Algorithm Demo ===");

        List<Server> servers = createTestServers();
        LoadBalancer loadBalancer = LoadBalancerFactory.createLoadBalancer(
                LoadBalancerFactory.AlgorithmType.LEAST_ACTIVE_CONNECTIONS, servers);

        // Simulate concurrent requests
        ExecutorService executor = Executors.newFixedThreadPool(5);

        for (int i = 1; i <= 20; i++) {
            final int requestId = i;
            executor.submit(() -> {
                String response = loadBalancer.processRequest("REQ-" + requestId);
                System.out.println("Request " + requestId + ": " + response);
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\nStatistics:\n" + loadBalancer.getStatistics());
    }

    /**
     * Demonstrates Least Response Time algorithm.
     */
    public void demoLeastResponseTime() {
        System.out.println("\n=== Least Response Time Algorithm Demo ===");

        List<Server> servers = createTestServers();
        LoadBalancer loadBalancer = LoadBalancerFactory.createLoadBalancer(
                LoadBalancerFactory.AlgorithmType.LEAST_RESPONSE_TIME, servers);

        // Process requests to build response time history
        for (int i = 1; i <= 30; i++) {
            String response = loadBalancer.processRequest("REQ-" + i);
            System.out.println("Request " + i + ": " + response);

            // Add some delay to see the algorithm in action
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println("\nStatistics:\n" + loadBalancer.getStatistics());
    }

    /**
     * Creates a list of test servers with equal weights.
     */
    private List<Server> createTestServers() {
        List<Server> servers = new ArrayList<>();
        servers.add(new Server("server-1", "192.168.1.10", 8080));
        servers.add(new Server("server-2", "192.168.1.11", 8080));
        servers.add(new Server("server-3", "192.168.1.12", 8080));
        return servers;
    }

    /**
     * Creates a list of test servers with different weights.
     */
    private List<Server> createWeightedTestServers() {
        List<Server> servers = new ArrayList<>();
        servers.add(new Server("server-1", "192.168.1.10", 8080, 1)); // Weight 1
        servers.add(new Server("server-2", "192.168.1.11", 8080, 2)); // Weight 2
        servers.add(new Server("server-3", "192.168.1.12", 8080, 3)); // Weight 3
        return servers;
    }
}