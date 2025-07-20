# RabbitMQ Producer and Consumer Demo

This project demonstrates RabbitMQ producer and consumer functionality in Java, including queue and exchange setup with different routing patterns.

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- RabbitMQ (running on localhost:5672)

## Project Structure

```
rabbitmq-demo/
├── pom.xml                          # Maven dependencies
├── docker-compose.yml               # RabbitMQ setup with Docker
├── run-demo.sh                      # Interactive demo runner
├── README.md                        # This file
└── src/main/java/com/example/rabbitmq/
    ├── RabbitMQDemo.java           # Main demo class (producer + consumer)
    ├── RabbitMQProducerDemo.java   # Standalone producer
    ├── RabbitMQConsumerDemo.java   # Standalone consumer
    └── QueueSetup.java             # Queue and exchange management utility
```

## Setup RabbitMQ

### Option 1: Using Docker (Recommended)

```bash
# Start RabbitMQ with Management UI
docker-compose up -d
```

This will start:

- RabbitMQ server on port 5672
- Management UI on port 15672 (http://localhost:15672)
- RabbitMQ Exporter on port 9090

### Option 2: Using RabbitMQ Binary

1. Download RabbitMQ from https://www.rabbitmq.com/download.html
2. Install and start RabbitMQ server
3. Enable management plugin: `rabbitmq-plugins enable rabbitmq_management`

## Building the Project

```bash
mvn clean compile
```

## Running the Demo

### Option 1: Run Complete Demo (Producer + Consumer)

This runs both producer and consumer in separate threads:

```bash
mvn exec:java -Dexec.mainClass="com.example.rabbitmq.RabbitMQDemo"
```

### Option 2: Run Producer and Consumer Separately

**Terminal 1 - Start Consumer:**

```bash
mvn exec:java -Dexec.mainClass="com.example.rabbitmq.RabbitMQConsumerDemo"
```

**Terminal 2 - Start Producer:**

```bash
mvn exec:java -Dexec.mainClass="com.example.rabbitmq.RabbitMQProducerDemo"
```

### Option 3: Setup Queues and Exchanges Only

```bash
mvn exec:java -Dexec.mainClass="com.example.rabbitmq.QueueSetup"
```

## Features

### Queue and Exchange Management

- Creates Direct Exchange with routing key
- Creates Fanout Exchange for broadcast messages
- Creates Topic Exchange for pattern-based routing
- Sets up durable queues with proper bindings

### Producer Features

- Publishes messages to exchanges with routing keys
- Uses publisher confirms for delivery guarantees
- Persistent message delivery
- Configurable message properties

### Consumer Features

- Subscribes to queues with manual acknowledgments
- Quality of Service (QoS) settings
- Message delivery callbacks
- Graceful error handling

## Exchange Types Demonstrated

### 1. Direct Exchange

- **Exchange**: `demo-exchange`
- **Queue**: `demo-queue`
- **Routing Key**: `demo.routing.key`
- **Use Case**: Point-to-point messaging

### 2. Fanout Exchange

- **Exchange**: `fanout-exchange`
- **Queues**: `fanout-queue-1`, `fanout-queue-2`
- **Routing Key**: Not used (broadcast)
- **Use Case**: Broadcast messages to multiple consumers

### 3. Topic Exchange

- **Exchange**: `topic-exchange`
- **Queues**: `topic-queue-orders`, `topic-queue-notifications`, `topic-queue-all`
- **Routing Patterns**:
  - `orders.*` → `topic-queue-orders`
  - `*.notification` → `topic-queue-notifications`
  - `#` → `topic-queue-all` (all messages)
- **Use Case**: Pattern-based message routing

## Configuration

Key configuration parameters in the code:

- **Host**: `localhost`
- **Port**: `5672`
- **Username**: `guest`
- **Password**: `guest`
- **Queue Name**: `demo-queue`
- **Exchange Name**: `demo-exchange`
- **Routing Key**: `demo.routing.key`

## Expected Output

### Producer Output:

```
Starting RabbitMQ Producer Demo...
Producer: Message 1 sent successfully
Producer: Message 2 sent successfully
...
```

### Consumer Output:

```
Starting RabbitMQ Consumer Demo...
Consumer waiting for messages... (Press Ctrl+C to stop)
Consumer started with tag: amq.ctag-...
Consumer: Received message - exchange=demo-exchange, routingKey=demo.routing.key, deliveryTag=1, message=Message 1 from producer at ...
Consumer: Received message - exchange=demo-exchange, routingKey=demo.routing.key, deliveryTag=2, message=Message 2 from producer at ...
...
```

## Management UI

Access RabbitMQ Management UI at http://localhost:15672

- **Username**: `guest`
- **Password**: `guest`

Features available:

- View queues, exchanges, and bindings
- Monitor message rates and queue depths
- Publish and consume messages manually
- View connection and channel information

## Troubleshooting

### Common Issues:

1. **Connection Refused**: Make sure RabbitMQ is running on localhost:5672
2. **Queue Not Found**: Run QueueSetup first to create queues and exchanges
3. **Consumer Not Receiving Messages**: Check if producer is running and routing keys match

### Debug Commands:

```bash
# Check RabbitMQ status
docker-compose ps

# View RabbitMQ logs
docker-compose logs rabbitmq

# Access RabbitMQ CLI
docker exec -it rabbitmq rabbitmqctl list_queues
docker exec -it rabbitmq rabbitmqctl list_exchanges
```

## Advanced Usage

### Custom Exchange Types

Modify `QueueSetup.java` to create different exchange types:

```java
// Create headers exchange
channel.exchangeDeclare("headers-exchange", BuiltinExchangeType.HEADERS, true, false, null);

// Create custom exchange with arguments
Map<String, Object> args = new HashMap<>();
args.put("alternate-exchange", "backup-exchange");
channel.exchangeDeclare("main-exchange", BuiltinExchangeType.DIRECT, true, false, args);
```

### Multiple Consumers

Run multiple instances of `RabbitMQConsumerDemo` to see load balancing across consumers.

### Message Persistence

Messages are marked as persistent by default. To change:

```java
// Non-persistent message
channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY,
    MessageProperties.BASIC,
    message.getBytes(StandardCharsets.UTF_8));
```

### Dead Letter Queues

Set up dead letter queues for failed message handling:

```java
Map<String, Object> args = new HashMap<>();
args.put("x-dead-letter-exchange", "dlx");
args.put("x-dead-letter-routing-key", "dlq");
channel.queueDeclare("main-queue", true, false, false, args);
```

## Dependencies

- RabbitMQ Java Client 5.18.0
- SLF4J for logging
- Jackson for JSON processing (if needed)

## License

This project is for educational purposes. Feel free to modify and use as needed.
