package com.example.loadbalancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Weighted Round Robin load balancing algorithm.
 * Distributes requests based on server weights, giving more requests to servers
 * with higher weights.
 */
public class WeightedRoundRobinAlgorithm implements LoadBalancingAlgorithm {

    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final AtomicInteger currentWeight = new AtomicInteger(0);

    @Override
    public Server selectServer(List<Server> servers) {
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

        // Find the maximum weight among all servers
        int maxWeight = healthyServers.stream()
                .mapToInt(Server::getWeight)
                .max()
                .orElse(1);

        // Find the greatest common divisor of all weights
        int gcd = calculateGCD(healthyServers.stream()
                .mapToInt(Server::getWeight)
                .toArray());

        while (true) {
            int index = currentIndex.get();
            int weight = currentWeight.get();

            if (weight == 0) {
                weight = maxWeight;
                currentWeight.set(weight);
            }

            Server server = healthyServers.get(index);

            if (server.getWeight() >= weight) {
                // Move to next server
                currentIndex.set((index + 1) % healthyServers.size());
                currentWeight.set(weight - gcd);
                return server;
            } else {
                // Move to next server without selecting current one
                currentIndex.set((index + 1) % healthyServers.size());
                currentWeight.set(weight - gcd);
            }
        }
    }

    private int calculateGCD(int[] weights) {
        if (weights.length == 0)
            return 1;

        int gcd = weights[0];
        for (int i = 1; i < weights.length; i++) {
            gcd = calculateGCD(gcd, weights[i]);
        }
        return gcd;
    }

    private int calculateGCD(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    @Override
    public String getAlgorithmName() {
        return "Weighted Round Robin";
    }
}