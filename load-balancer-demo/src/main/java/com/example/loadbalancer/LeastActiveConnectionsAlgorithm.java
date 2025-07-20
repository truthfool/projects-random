package com.example.loadbalancer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Least Active Connections load balancing algorithm.
 * Selects the server with the fewest active connections.
 */
public class LeastActiveConnectionsAlgorithm implements LoadBalancingAlgorithm {

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

        // Find server with least active connections
        return healthyServers.stream()
                .min((s1, s2) -> {
                    int connections1 = s1.getActiveConnections();
                    int connections2 = s2.getActiveConnections();

                    if (connections1 != connections2) {
                        return Integer.compare(connections1, connections2);
                    }

                    // If connections are equal, use request count as tiebreaker
                    return Long.compare(s1.getRequestCount(), s2.getRequestCount());
                })
                .orElse(null);
    }

    @Override
    public String getAlgorithmName() {
        return "Least Active Connections";
    }
}