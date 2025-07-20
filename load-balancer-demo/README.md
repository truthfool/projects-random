# Load Balancer Demo

A Java implementation of a load balancer with multiple algorithms that can be selected using a factory pattern.

## Features

- **Multiple Load Balancing Algorithms:**

  - Round Robin
  - Weighted Round Robin
  - Least Active Connections
  - Least Response Time

- **Factory Pattern:** Easy algorithm selection through `LoadBalancerFactory`
- **Thread-Safe:** Uses concurrent collections and atomic operations
- **Health Monitoring:** Tracks server health status
- **Statistics:** Provides detailed statistics about server performance
- **Extensible:** Easy to add new algorithms by implementing `LoadBalancingAlgorithm` interface

## Architecture

### Core Classes

1. **`Server`** - Represents a backend server with metrics tracking
2. **`LoadBalancingAlgorithm`** - Interface for load balancing strategies
3. **`LoadBalancer`** - Main load balancer class that uses the selected algorithm
4. **`LoadBalancerFactory`** - Factory class for creating load balancers with different algorithms

### Algorithm Implementations

- **`RoundRobinAlgorithm`** - Distributes requests evenly in a circular manner
- **`WeightedRoundRobinAlgorithm`** - Distributes requests based on server weights
- **`LeastActiveConnectionsAlgorithm`** - Selects server with fewest active connections
- **`LeastResponseTimeAlgorithm`** - Selects server with lowest average response time

## Usage Examples

### Basic Usage

```java
// Create servers
List<Server> servers = new ArrayList<>();
servers.add(new Server("server-1", "192.168.1.10", 8080));
servers.add(new Server("server-2", "192.168.1.11", 8080));
servers.add(new Server("server-3", "192.168.1.12", 8080));

// Create load balancer with Round Robin algorithm
LoadBalancer loadBalancer = LoadBalancerFactory.createLoadBalancer("round_robin", servers);

// Get next server
Server server = loadBalancer.getNextServer();

// Process a request
String response = loadBalancer.processRequest("REQ-001");
```

### Using Different Algorithms

```java
// Round Robin
LoadBalancer roundRobin = LoadBalancerFactory.createLoadBalancer("round_robin", servers);

// Weighted Round Robin
LoadBalancer weighted = LoadBalancerFactory.createLoadBalancer("weighted_round_robin", servers);

// Least Active Connections
LoadBalancer leastConnections = LoadBalancerFactory.createLoadBalancer("least_active_connections", servers);

// Least Response Time
LoadBalancer leastResponseTime = LoadBalancerFactory.createLoadBalancer("least_response_time", servers);
```

### Using Enum Instead of String

```java
LoadBalancer loadBalancer = LoadBalancerFactory.createLoadBalancer(
    LoadBalancerFactory.AlgorithmType.ROUND_ROBIN, servers);
```

### Weighted Servers

```java
// Create servers with different weights
List<Server> servers = new ArrayList<>();
servers.add(new Server("server-1", "192.168.1.10", 8080, 1));  // Weight 1
servers.add(new Server("server-2", "192.168.1.11", 8080, 2));  // Weight 2
servers.add(new Server("server-3", "192.168.1.12", 8080, 3));  // Weight 3

// Use Weighted Round Robin
LoadBalancer loadBalancer = LoadBalancerFactory.createLoadBalancer("weighted_round_robin", servers);
```

### Getting Statistics

```java
LoadBalancer loadBalancer = LoadBalancerFactory.createLoadBalancer("round_robin", servers);

// Process some requests
for (int i = 1; i <= 10; i++) {
    loadBalancer.processRequest("REQ-" + i);
}

// Get statistics
String stats = loadBalancer.getStatistics();
System.out.println(stats);
```

## Building and Running

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

### Build the Project

```bash
cd load-balancer-demo
mvn clean compile
```

### Run the Demo

```bash
# Run the comprehensive demo
mvn exec:java -Dexec.mainClass="com.example.loadbalancer.LoadBalancerDemo"

# Run the simple example
mvn exec:java -Dexec.mainClass="com.example.loadbalancer.SimpleExample"
```

### Run Tests

```bash
mvn test
```

## Algorithm Details

### Round Robin

- Distributes requests evenly across all healthy servers
- Simple and predictable
- Good for servers with similar capabilities

### Weighted Round Robin

- Distributes requests based on server weights
- Higher weight servers receive more requests
- Useful when servers have different capacities

### Least Active Connections

- Selects server with fewest active connections
- Good for long-lived connections
- Helps balance load based on current server load

### Least Response Time

- Selects server with lowest average response time
- Good for optimizing user experience
- Automatically adapts to server performance

## Extending the Load Balancer

### Adding a New Algorithm

1. Implement the `LoadBalancingAlgorithm` interface:

```java
public class CustomAlgorithm implements LoadBalancingAlgorithm {
    @Override
    public Server selectServer(List<Server> servers) {
        // Your algorithm logic here
        return selectedServer;
    }

    @Override
    public String getAlgorithmName() {
        return "Custom Algorithm";
    }
}
```

2. Add the new algorithm to the factory:

```java
// In LoadBalancerFactory.java
public enum AlgorithmType {
    // ... existing types
    CUSTOM("custom");

    // In createAlgorithm method
    case CUSTOM:
        return new CustomAlgorithm();
}
```

### Server Health Checks

The load balancer automatically filters out unhealthy servers. You can mark servers as unhealthy:

```java
server.setHealthy(false);
```

## Thread Safety

The load balancer is designed to be thread-safe:

- Uses `CopyOnWriteArrayList` for server list
- Uses atomic operations for counters
- All algorithms are stateless and thread-safe

## Performance Considerations

- Algorithms are designed to be efficient with O(n) complexity where n is the number of servers
- Server metrics are updated atomically to avoid race conditions
- The load balancer uses minimal memory overhead

## License

This project is provided as a demonstration and can be used for educational purposes.
