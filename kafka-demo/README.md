# Kafka Producer and Consumer Demo

This project demonstrates Apache Kafka producer and consumer functionality in Java, including topic creation with partitions.

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Apache Kafka (running on localhost:9092)

## Project Structure

```
kafka-demo/
├── pom.xml                          # Maven dependencies
├── src/main/java/com/example/kafka/
│   ├── KafkaDemo.java              # Main demo class (producer + consumer)
│   ├── KafkaProducerDemo.java      # Standalone producer
│   ├── KafkaConsumerDemo.java      # Standalone consumer
│   └── TopicSetup.java             # Topic management utility
└── README.md                       # This file
```

## Setup Kafka

### Option 1: Using Docker (Recommended)

```bash
# Start Kafka with Zookeeper
docker run -d --name zookeeper -p 2181:2181 confluentinc/cp-zookeeper:latest

docker run -d --name kafka \
  -p 9092:9092 \
  -e KAFKA_ZOOKEEPER_CONNECT=localhost:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka:latest
```

### Option 2: Using Kafka Binary

1. Download Kafka from https://kafka.apache.org/download
2. Extract and navigate to the Kafka directory
3. Start Zookeeper: `bin/zookeeper-server-start.sh config/zookeeper.properties`
4. Start Kafka: `bin/kafka-server-start.sh config/server.properties`

## Building the Project

```bash
mvn clean compile
```

## Running the Demo

### Option 1: Run Complete Demo (Producer + Consumer)

This runs both producer and consumer in separate threads:

```bash
mvn exec:java -Dexec.mainClass="com.example.kafka.KafkaDemo"
```

### Option 2: Run Producer and Consumer Separately

**Terminal 1 - Start Consumer:**

```bash
mvn exec:java -Dexec.mainClass="com.example.kafka.KafkaConsumerDemo"
```

**Terminal 2 - Start Producer:**

```bash
mvn exec:java -Dexec.mainClass="com.example.kafka.KafkaProducerDemo"
```

### Option 3: Setup Topics Only

```bash
mvn exec:java -Dexec.mainClass="com.example.kafka.TopicSetup"
```

## Features

### Topic Management

- Creates topic `demo-topic` with 3 partitions
- Sets retention period to 24 hours
- Configures cleanup policy and compression

### Producer Features

- Sends messages with keys and values
- Uses async callbacks for delivery confirmation
- Configurable batch size and linger time
- Automatic retries on failure

### Consumer Features

- Subscribes to topic with consumer group
- Auto-commits offsets
- Reads from earliest offset
- Displays partition and offset information

## Configuration

Key configuration parameters in the code:

- **Bootstrap Servers**: `localhost:9092`
- **Topic Name**: `demo-topic`
- **Consumer Group**: `demo-consumer-group`
- **Partitions**: 3
- **Replication Factor**: 1

## Expected Output

### Producer Output:

```
Starting Kafka Producer Demo...
Producer: Message sent successfully to topic=demo-topic, partition=0, offset=0, key=key-1
Producer: Message sent successfully to topic=demo-topic, partition=1, offset=0, key=key-2
...
```

### Consumer Output:

```
Starting Kafka Consumer Demo...
Consumer subscribed to topic: demo-topic
Waiting for messages...
Consumer: Received message #1 - topic=demo-topic, partition=0, offset=0, key=key-1, value=Message 1 from producer at ...
Consumer: Received message #2 - topic=demo-topic, partition=1, offset=0, key=key-2, value=Message 2 from producer at ...
...
```

## Troubleshooting

### Common Issues:

1. **Connection Refused**: Make sure Kafka is running on localhost:9092
2. **Topic Not Found**: Run TopicSetup first to create the topic
3. **Consumer Not Receiving Messages**: Check if producer is running and topic exists

### Debug Commands:

```bash
# List topics
kafka-topics.sh --list --bootstrap-server localhost:9092

# Describe topic
kafka-topics.sh --describe --topic demo-topic --bootstrap-server localhost:9092

# Consume messages from command line
kafka-console-consumer.sh --topic demo-topic --from-beginning --bootstrap-server localhost:9092
```

## Advanced Usage

### Custom Topic Configuration

Modify `TopicSetup.java` to create topics with different configurations:

```java
// Create topic with custom settings
setup.createTopic("my-topic", 5, (short) 2); // 5 partitions, replication factor 2
```

### Multiple Consumers

Run multiple instances of `KafkaConsumerDemo` to see load balancing across partitions.

### Custom Message Format

Modify the producer to send JSON or custom objects by changing serializers in the configuration.

## Dependencies

- Apache Kafka Clients 3.5.1
- SLF4J for logging
- Jackson for JSON processing (if needed)

## License

This project is for educational purposes. Feel free to modify and use as needed.
