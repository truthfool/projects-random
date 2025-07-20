package com.example.loadbalancer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Main load balancer class that distributes requests across multiple servers
 * using the specified load balancing algorithm.
 */
public class LoadBalancer {

    private final LoadBalancingAlgorithm algorithm;
    private final List<Server> servers;

    public LoadBalancer(LoadBalancingAlgorithm algorithm, List<Server> servers) {
        this.algorithm = algorithm;
        this.servers = new CopyOnWriteArrayList<>(servers);
        System.out.println("Load balancer initialized with " + algorithm.getAlgorithmName() + " algorithm and "
                + servers.size() + " servers");
    }

    /**
     * Gets the next server using the configured algorithm.
     * 
     * @return Selected server, or null if no servers are available
     */
    public Server getNextServer() {
        Server selectedServer = algorithm.selectServer(servers);

        if (selectedServer != null) {
            selectedServer.incrementActiveConnections();
            System.out.println("Selected server: " + selectedServer.getId());
        } else {
            System.out.println("No healthy servers available");
        }

        return selectedServer;
    }

    /**
     * Simulates a request to the selected server and records response time.
     * 
     * @param requestId Unique identifier for the request
     * @return Response from the server
     */
    public String processRequest(String requestId) {
        Server server = getNextServer();

        if (server == null) {
            return "Error: No servers available";
        }

        try {
            long startTime = System.currentTimeMillis();

            // Simulate processing time (in real scenario, this would be an actual HTTP
            // request)
            String response = simulateServerResponse(server, requestId);

            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;

            // Record response time
            server.recordResponseTime(responseTime);

            System.out.println(
                    "Request " + requestId + " processed by server " + server.getId() + " in " + responseTime + "ms");

            return response;

        } finally {
            // Always decrement active connections
            server.decrementActiveConnections();
        }
    }

    /**
     * Simulates a server response (in a real scenario, this would be an HTTP
     * request).
     * 
     * @param server    The server to send the request to
     * @param requestId The request identifier
     * @return Simulated response
     */
    private String simulateServerResponse(Server server, String requestId) {
        try {
            // Simulate network latency and processing time
            Thread.sleep((long) (Math.random() * 100) + 10);

            return String.format("Response from %s:%d for request %s",
                    server.getHost(), server.getPort(), requestId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error: Request interrupted";
        }
    }

    /**
     * Adds a new server to the load balancer.
     * 
     * @param server The server to add
     */
    public void addServer(Server server) {
        servers.add(server);
        System.out.println("Added server: " + server.getId());
    }

    /**
     * Removes a server from the load balancer.
     * 
     * @param server The server to remove
     */
    public void removeServer(Server server) {
        servers.remove(server);
        System.out.println("Removed server: " + server.getId());
    }

    /**
     * Gets the current list of servers.
     * 
     * @return List of servers
     */
    public List<Server> getServers() {
        return new CopyOnWriteArrayList<>(servers);
    }

    /**
     * Gets the algorithm name.
     * 
     * @return Algorithm name
     */
    public String getAlgorithmName() {
        return algorithm.getAlgorithmName();
    }

    /**
     * Gets statistics about the load balancer.
     * 
     * @return Statistics string
     */
    public String getStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("Load Balancer Statistics:\n");
        stats.append("Algorithm: ").append(getAlgorithmName()).append("\n");
        stats.append("Total Servers: ").append(servers.size()).append("\n");
        stats.append("Healthy Servers: ").append(servers.stream().filter(Server::isHealthy).count()).append("\n\n");

        stats.append("Server Details:\n");
        for (Server server : servers) {
            stats.append(String.format("  %s: Active=%d, Requests=%d, AvgResponseTime=%dms, Healthy=%s\n",
                    server.getId(), server.getActiveConnections(), server.getRequestCount(),
                    server.getAverageResponseTime(), server.isHealthy()));
        }

        return stats.toString();
    }
}