package com.example.loadbalancer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Weighted Round Robin load balancing algorithm.
 * 
 * Simple Implementation:
 * We stick to one server until we have served 'weight' number of requests,
 * then we move to the next server.
 * 
 * Example: Server A (Weight 3), Server B (Weight 1)
 * Sequence: A, A, A, B, A, A, A, B ...
 */
public class WeightedRoundRobinAlgorithm implements LoadBalancingAlgorithm {

    private int currentIndex = 0;
    private int currentCount = 0;

    @Override
    public synchronized Server selectServer(List<Server> servers) {
        if (servers == null || servers.isEmpty()) {
            return null;
        }

        // Filter only healthy servers
        List<Server> healthyServers = servers.stream()
                .filter(Server::isHealthy)
                .collect(Collectors.toList());

        if (healthyServers.isEmpty()) {
            return null;
        }

        // Safety check: specific case if servers changed and index is out of bounds
        if (currentIndex >= healthyServers.size()) {
            currentIndex = 0;
            currentCount = 0;
        }

        Server server = healthyServers.get(currentIndex);

        // Logic: Have we served enough requests for this server?
        // If currentCount is less than weight, continue using this server.
        if (currentCount < server.getWeight()) {
            currentCount++;
            return server;
        }

        // If we reached the limit for this server, move to the next one
        currentIndex = (currentIndex + 1) % healthyServers.size();
        currentCount = 1; // Start counting for the new server (this is the first request)

        return healthyServers.get(currentIndex);
    }

    @Override
    public String getAlgorithmName() {
        return "Weighted Round Robin (Simple)";
    }
}