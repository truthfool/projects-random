package com.example.loadbalancer;

import java.util.List;

/**
 * Factory class for creating load balancers with different algorithms.
 */
public class LoadBalancerFactory {

    /**
     * Enumeration of available load balancing algorithms.
     */
    public enum AlgorithmType {
        ROUND_ROBIN("round_robin"),
        WEIGHTED_ROUND_ROBIN("weighted_round_robin"),
        LEAST_ACTIVE_CONNECTIONS("least_active_connections"),
        LEAST_RESPONSE_TIME("least_response_time");

        private final String value;

        AlgorithmType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static AlgorithmType fromString(String text) {
            for (AlgorithmType type : AlgorithmType.values()) {
                if (type.getValue().equalsIgnoreCase(text)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown algorithm type: " + text);
        }
    }

    /**
     * Creates a load balancer with the specified algorithm.
     * 
     * @param algorithmType The type of algorithm to use
     * @param servers       List of servers to load balance
     * @return LoadBalancer instance
     */
    public static LoadBalancer createLoadBalancer(AlgorithmType algorithmType, List<Server> servers) {
        LoadBalancingAlgorithm algorithm = createAlgorithm(algorithmType);
        return new LoadBalancer(algorithm, servers);
    }

    /**
     * Creates a load balancer with the specified algorithm using string input.
     * 
     * @param algorithmType The algorithm type as string
     * @param servers       List of servers to load balance
     * @return LoadBalancer instance
     */
    public static LoadBalancer createLoadBalancer(String algorithmType, List<Server> servers) {
        AlgorithmType type = AlgorithmType.fromString(algorithmType);
        return createLoadBalancer(type, servers);
    }

    /**
     * Creates the appropriate algorithm instance based on the algorithm type.
     * 
     * @param algorithmType The type of algorithm to create
     * @return LoadBalancingAlgorithm instance
     */
    private static LoadBalancingAlgorithm createAlgorithm(AlgorithmType algorithmType) {
        switch (algorithmType) {
            case ROUND_ROBIN:
                return new RoundRobinAlgorithm();
            case WEIGHTED_ROUND_ROBIN:
                return new WeightedRoundRobinAlgorithm();
            case LEAST_ACTIVE_CONNECTIONS:
                return new LeastActiveConnectionsAlgorithm();
            case LEAST_RESPONSE_TIME:
                return new LeastResponseTimeAlgorithm();
            default:
                throw new IllegalArgumentException("Unsupported algorithm type: " + algorithmType);
        }
    }
}