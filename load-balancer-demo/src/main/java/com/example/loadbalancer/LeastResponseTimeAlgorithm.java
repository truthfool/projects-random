package com.example.loadbalancer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Least Response Time load balancing algorithm.
 * Selects the server with the lowest average response time.
 */
public class LeastResponseTimeAlgorithm implements LoadBalancingAlgorithm {

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

        // Find server with least response time
        return healthyServers.stream()
                .min((s1, s2) -> {
                    long responseTime1 = s1.getAverageResponseTime();
                    long responseTime2 = s2.getAverageResponseTime();

                    if (responseTime1 != responseTime2) {
                        return Long.compare(responseTime1, responseTime2);
                    }

                    // If response times are equal, use active connections as tiebreaker
                    return Integer.compare(s1.getActiveConnections(), s2.getActiveConnections());
                })
                .orElse(null);
    }

    @Override
    public String getAlgorithmName() {
        return "Least Response Time";
    }
}