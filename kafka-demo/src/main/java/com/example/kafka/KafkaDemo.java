package com.example.kafka;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.config.TopicConfig;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class KafkaDemo {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC_NAME = "demo-topic";
    private static final String GROUP_ID = "demo-consumer-group";
    private static final int NUM_PARTITIONS = 3;
    private static final short REPLICATION_FACTOR = 1;

    public static void main(String[] args) {
        KafkaDemo demo = new KafkaDemo();

        try {
            // Setup Kafka topic
            demo.setupTopic();

            // Run producer and consumer in separate threads
            demo.runDemo();

        } catch (Exception e) {
            System.err.println("Error running Kafka demo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setupTopic() throws ExecutionException, InterruptedException {
        System.out.println("Setting up Kafka topic: " + TOPIC_NAME);

        Properties adminProps = new Properties();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

        try (AdminClient adminClient = AdminClient.create(adminProps)) {

            // Check if topic already exists
            ListTopicsResult topics = adminClient.listTopics();
            Set<String> existingTopics = topics.names().get();

            if (existingTopics.contains(TOPIC_NAME)) {
                System.out.println("Topic " + TOPIC_NAME + " already exists");
                return;
            }

            // Create topic with partitions
            NewTopic newTopic = new NewTopic(TOPIC_NAME, NUM_PARTITIONS, REPLICATION_FACTOR);

            // Set topic configuration
            Map<String, String> topicConfigs = new HashMap<>();
            topicConfigs.put(TopicConfig.RETENTION_MS_CONFIG, "86400000"); // 24 hours
            topicConfigs.put(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE);
            newTopic.configs(topicConfigs);

            CreateTopicsResult result = adminClient.createTopics(Collections.singleton(newTopic));
            result.all().get();

            System.out.println("Successfully created topic: " + TOPIC_NAME + " with " + NUM_PARTITIONS + " partitions");

        } catch (Exception e) {
            System.err.println("Error creating topic: " + e.getMessage());
            throw e;
        }
    }

    public void runDemo() {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Start consumer first
        executor.submit(this::runConsumer);

        // Wait a bit for consumer to start
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Start producer
        executor.submit(this::runProducer);

        // Shutdown after some time
        try {
            Thread.sleep(10000); // Run for 10 seconds
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void runProducer() {
        System.out.println("Starting Kafka Producer...");

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {

            for (int i = 1; i <= 10; i++) {
                String key = "key-" + i;
                String value = "Message " + i + " from producer at " + new Date();

                ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, key, value);

                producer.send(record, (metadata, exception) -> {
                    if (exception == null) {
                        System.out.println("Producer: Message sent successfully to topic=" + metadata.topic() +
                                ", partition=" + metadata.partition() +
                                ", offset=" + metadata.offset() +
                                ", key=" + key);
                    } else {
                        System.err.println("Producer: Error sending message: " + exception.getMessage());
                    }
                });

                Thread.sleep(1000); // Send message every second
            }

            producer.flush();
            System.out.println("Producer finished sending messages");

        } catch (Exception e) {
            System.err.println("Error in producer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void runConsumer() {
        System.out.println("Starting Kafka Consumer...");

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {

            consumer.subscribe(Collections.singletonList(TOPIC_NAME));

            System.out.println("Consumer subscribed to topic: " + TOPIC_NAME);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("Consumer: Received message - " +
                            "topic=" + record.topic() +
                            ", partition=" + record.partition() +
                            ", offset=" + record.offset() +
                            ", key=" + record.key() +
                            ", value=" + record.value());
                }
            }

        } catch (Exception e) {
            System.err.println("Error in consumer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}