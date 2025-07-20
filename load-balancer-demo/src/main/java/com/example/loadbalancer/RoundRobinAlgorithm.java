package com.example.loadbalancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Round Robin load balancing algorithm.
 * Distributes requests evenly across all healthy servers in a circular manner.
 */
public class RoundRobinAlgorithm implements LoadBalancingAlgorithm {

    private final AtomicInteger currentIndex = new AtomicInteger(0);

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

        // Get next server in round-robin fashion
        int index = currentIndex.getAndIncrement() % healthyServers.size();
        return healthyServers.get(index);
    }

    @Override
    public String getAlgorithmName() {
        return "Round Robin";
    }
}