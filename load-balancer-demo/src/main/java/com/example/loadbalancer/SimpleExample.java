package com.example.loadbalancer;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple example showing how to use the LoadBalancerFactory.
 */
public class SimpleExample {

    public static void main(String[] args) {
        // Create some test servers
        List<Server> servers = new ArrayList<>();
        servers.add(new Server("web-1", "10.0.0.1", 8080, 2));
        servers.add(new Server("web-2", "10.0.0.2", 8080, 1));
        servers.add(new Server("web-3", "10.0.0.3", 8080, 3));

        // Example 1: Create a Round Robin load balancer
        System.out.println("=== Example 1: Round Robin ===");
        LoadBalancer roundRobinLB = LoadBalancerFactory.createLoadBalancer("round_robin", servers);

        for (int i = 1; i <= 5; i++) {
            Server server = roundRobinLB.getNextServer();
            System.out.println("Request " + i + " -> " + server.getId());
        }

        // Example 2: Create a Weighted Round Robin load balancer
        System.out.println("\n=== Example 2: Weighted Round Robin ===");
        LoadBalancer weightedLB = LoadBalancerFactory.createLoadBalancer("weighted_round_robin", servers);

        for (int i = 1; i <= 6; i++) {
            Server server = weightedLB.getNextServer();
            System.out.println("Request " + i + " -> " + server.getId() + " (weight: " + server.getWeight() + ")");
        }

        // Example 3: Create a Least Active Connections load balancer
        System.out.println("\n=== Example 3: Least Active Connections ===");
        LoadBalancer leastConnectionsLB = LoadBalancerFactory.createLoadBalancer("least_active_connections", servers);

        // Simulate some active connections
        servers.get(0).incrementActiveConnections();
        servers.get(0).incrementActiveConnections();
        servers.get(1).incrementActiveConnections();

        for (int i = 1; i <= 3; i++) {
            Server server = leastConnectionsLB.getNextServer();
            System.out.println("Request " + i + " -> " + server.getId() + " (active connections: "
                    + server.getActiveConnections() + ")");
        }

        // Example 4: Create a Least Response Time load balancer
        System.out.println("\n=== Example 4: Least Response Time ===");
        LoadBalancer leastResponseTimeLB = LoadBalancerFactory.createLoadBalancer("least_response_time", servers);

        // Simulate some response times
        servers.get(0).recordResponseTime(100);
        servers.get(1).recordResponseTime(50);
        servers.get(2).recordResponseTime(200);

        for (int i = 1; i <= 3; i++) {
            Server server = leastResponseTimeLB.getNextServer();
            System.out.println("Request " + i + " -> " + server.getId() + " (avg response time: "
                    + server.getAverageResponseTime() + "ms)");
        }

        // Example 5: Using enum instead of string
        System.out.println("\n=== Example 5: Using Enum ===");
        LoadBalancer enumLB = LoadBalancerFactory.createLoadBalancer(
                LoadBalancerFactory.AlgorithmType.ROUND_ROBIN, servers);

        System.out.println("Created load balancer with algorithm: " + enumLB.getAlgorithmName());

        // Example 6: Process requests and get statistics
        System.out.println("\n=== Example 6: Processing Requests ===");
        LoadBalancer processingLB = LoadBalancerFactory.createLoadBalancer("round_robin", servers);

        for (int i = 1; i <= 3; i++) {
            String response = processingLB.processRequest("REQ-" + i);
            System.out.println("Response " + i + ": " + response);
        }

        System.out.println("\nFinal Statistics:");
        System.out.println(processingLB.getStatistics());
    }
}