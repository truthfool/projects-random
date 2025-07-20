package com.example.loadbalancer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a backend server in the load balancer.
 */
public class Server {
    private final String id;
    private final String host;
    private final int port;
    private final int weight;
    private final AtomicInteger activeConnections;
    private final AtomicLong totalResponseTime;
    private final AtomicLong requestCount;
    private boolean healthy;

    public Server(String id, String host, int port) {
        this(id, host, port, 1);
    }

    public Server(String id, String host, int port, int weight) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.weight = weight;
        this.activeConnections = new AtomicInteger(0);
        this.totalResponseTime = new AtomicLong(0);
        this.requestCount = new AtomicLong(0);
        this.healthy = true;
    }

    public String getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getWeight() {
        return weight;
    }

    public int getActiveConnections() {
        return activeConnections.get();
    }

    public long getAverageResponseTime() {
        long count = requestCount.get();
        return count > 0 ? totalResponseTime.get() / count : 0;
    }

    public long getRequestCount() {
        return requestCount.get();
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public void incrementActiveConnections() {
        activeConnections.incrementAndGet();
    }

    public void decrementActiveConnections() {
        activeConnections.decrementAndGet();
    }

    public void recordResponseTime(long responseTime) {
        totalResponseTime.addAndGet(responseTime);
        requestCount.incrementAndGet();
    }

    @Override
    public String toString() {
        return String.format(
                "Server{id='%s', host='%s', port=%d, weight=%d, activeConnections=%d, avgResponseTime=%dms, healthy=%s}",
                id, host, port, weight, activeConnections.get(), getAverageResponseTime(), healthy);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Server server = (Server) obj;
        return id.equals(server.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}