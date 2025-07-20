package com.example.loadbalancer;

import java.util.List;

/**
 * Interface for load balancing algorithms.
 */
public interface LoadBalancingAlgorithm {

    /**
     * Selects the next server from the list of available servers.
     * 
     * @param servers List of available servers
     * @return Selected server, or null if no servers are available
     */
    Server selectServer(List<Server> servers);

    /**
     * Gets the name of the algorithm.
     * 
     * @return Algorithm name
     */
    String getAlgorithmName();
}