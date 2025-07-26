# Consistent Hashing Demo

A scalable Java project demonstrating consistent hashing with concurrent request handling, built with Spring Boot.

## Overview

This project implements a robust consistent hashing algorithm that provides:

- **Scalability**: Handles multiple concurrent requests efficiently
- **Fault Tolerance**: Gracefully handles node failures with minimal data redistribution
- **Load Balancing**: Distributes keys evenly across nodes using virtual nodes
- **REST API**: Complete HTTP API for managing nodes and performing lookups
- **Monitoring**: Built-in statistics and health monitoring

## Features

### Core Features

- **Virtual Node Ring**: Uses virtual nodes for better distribution
- **Thread-Safe Operations**: Concurrent access with read-write locks
- **MurmurHash3**: High-performance hashing algorithm
- **Dynamic Node Management**: Add/remove nodes at runtime
- **Replication Support**: Get multiple nodes for data replication
- **Caching Layer**: In-memory cache for frequently accessed data

### API Features

- **RESTful Endpoints**: Complete HTTP API
- **Async Operations**: Non-blocking request handling
- **Validation**: Input validation and error handling
- **Health Monitoring**: System health and statistics
- **Cross-Origin Support**: CORS enabled for web applications

### Performance Features

- **High Throughput**: Optimized for high-concurrency scenarios
- **Low Latency**: Efficient hash ring lookups
- **Memory Efficient**: Minimal memory footprint
- **Async Processing**: Background task execution

## Architecture

### Components

1. **ConsistentHashRing**: Core consistent hashing implementation
2. **ConsistentHashingService**: Business logic and caching layer
3. **ConsistentHashingController**: REST API endpoints
4. **ConsistentHashingDemo**: Demonstration scenarios

### Key Classes

- `ConsistentHashRing`: Implements the hash ring with virtual nodes
- `ConsistentHashRing.Node`: Represents a physical node
- `ConsistentHashRing.VirtualNode`: Represents a virtual node
- `ConsistentHashRing.HashRingStats`: Statistics about the hash ring

## API Endpoints

### Node Management

#### Add Node

```http
POST /api/v1/consistent-hashing/nodes
Content-Type: application/json

{
  "nodeId": "node-1",
  "nodeAddress": "192.168.1.10:8080"
}
```

#### Remove Node

```http
DELETE /api/v1/consistent-hashing/nodes/{nodeId}
```

#### Get All Nodes

```http
GET /api/v1/consistent-hashing/nodes
```

#### Simulate Node Failure

```http
POST /api/v1/consistent-hashing/nodes/{nodeId}/fail
```

### Key Lookups

#### Get Node for Key

```http
GET /api/v1/consistent-hashing/lookup/{key}
```

#### Get Multiple Nodes for Replication

```http
GET /api/v1/consistent-hashing/lookup/{key}/replicas?count=3
```

#### Async Key Lookup

```http
GET /api/v1/consistent-hashing/lookup/{key}/async
```

### Cache Operations

#### Store Value

```http
POST /api/v1/consistent-hashing/cache
Content-Type: application/json

{
  "key": "user:123",
  "value": "John Doe"
}
```

#### Retrieve Value

```http
GET /api/v1/consistent-hashing/cache/{key}
```

#### Clear Cache

```http
DELETE /api/v1/consistent-hashing/cache
```

### Statistics and Monitoring

#### Get Hash Ring Statistics

```http
GET /api/v1/consistent-hashing/stats
```

#### Get Cache Statistics

```http
GET /api/v1/consistent-hashing/cache/stats
```

#### Health Check

```http
GET /api/v1/consistent-hashing/health
```

## Response Format

All API responses follow a consistent format:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    // Response data
  },
  "timestamp": 1640995200000
}
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Building the Project

```bash
# Clone the repository
git clone <repository-url>
cd consistent-hashing-demo

# Build the project
mvn clean compile

# Run tests
mvn test

# Package the application
mvn package
```

### Running the Application

```bash
# Run with Maven
mvn spring-boot:run

# Or run the JAR file
java -jar target/consistent-hashing-demo-1.0.0.jar
```

The application will start on `http://localhost:8080`

### Configuration

The application can be configured via `application.properties`:

```properties
# Server Configuration
server.port=8080

# Async Configuration
spring.task.execution.pool.core-size=10
spring.task.execution.pool.max-size=20

# Logging
logging.level.com.example.consistenthashing=DEBUG
```

## Usage Examples

### Basic Usage

```java
// Create a service instance
ConsistentHashingService service = new ConsistentHashingService();

// Add nodes
service.addNode("node-1", "192.168.1.10:8080");
service.addNode("node-2", "192.168.1.11:8080");

// Look up a key
ConsistentHashRing.Node node = service.getNodeForKey("user-123");
System.out.println("Key 'user-123' maps to node: " + node.getId());

// Get multiple nodes for replication
List<ConsistentHashRing.Node> replicas = service.getNodesForKey("important-data", 3);
```

### Async Usage

```java
// Async key lookup
CompletableFuture<ConsistentHashRing.Node> future = service.getNodeForKeyAsync("user-123");
ConsistentHashRing.Node node = future.get(5, TimeUnit.SECONDS);
```

### Caching

```java
// Store and retrieve from cache
service.putInCache("user:123", "John Doe");
Object user = service.getFromCache("user:123");
```

## Demo Scenarios

The application includes a demo component that runs various scenarios:

1. **Basic Demo**: Simple key lookups and node assignments
2. **Distribution Demo**: Shows key distribution across nodes
3. **Node Failure Demo**: Demonstrates fault tolerance
4. **Concurrent Requests Demo**: Tests concurrent access
5. **Performance Demo**: Measures lookup performance

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ConsistentHashingServiceTest

# Run with coverage
mvn test jacoco:report
```

### Test Coverage

The project includes comprehensive tests covering:

- Node addition and removal
- Key lookups and consistency
- Cache operations
- Async operations
- Concurrent access
- Edge cases and error handling
- Performance and stress testing

## Performance Characteristics

### Benchmarks

- **Lookup Performance**: ~100,000 lookups/second
- **Concurrent Requests**: Supports 1000+ concurrent requests
- **Memory Usage**: ~1MB per 1000 virtual nodes
- **Node Addition**: O(log n) time complexity
- **Key Lookup**: O(log n) time complexity

### Scalability

- **Virtual Nodes**: 150 virtual nodes per physical node (configurable)
- **Hash Ring**: Uses ConcurrentSkipListMap for efficient lookups
- **Thread Safety**: Read-write locks for concurrent access
- **Async Processing**: Non-blocking operations

## Monitoring and Observability

### Health Checks

```http
GET /api/v1/consistent-hashing/health
```

Response:

```json
{
  "success": true,
  "data": {
    "status": "UP",
    "totalRequests": 1234,
    "nodeCount": 5,
    "timestamp": 1640995200000
  }
}
```

### Statistics

```http
GET /api/v1/consistent-hashing/stats
```

Response:

```json
{
  "success": true,
  "data": {
    "physicalNodeCount": 5,
    "virtualNodeCount": 750,
    "virtualNodesPerPhysicalNode": 150,
    "nodeDistribution": {
      "node-1": 150,
      "node-2": 150,
      "node-3": 150,
      "node-4": 150,
      "node-5": 150
    }
  }
}
```

## Error Handling

The application provides comprehensive error handling:

- **Validation Errors**: Input validation with detailed error messages
- **Node Not Found**: Graceful handling of missing nodes
- **Concurrent Modification**: Thread-safe operations
- **System Errors**: Proper HTTP status codes and error messages

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Spring Boot team for the excellent framework
- Google Guava for the MurmurHash3 implementation
- The consistent hashing research community

## Support

For questions and support, please open an issue on the project repository.
