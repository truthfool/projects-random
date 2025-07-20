package com.example.kafka;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.config.TopicConfig;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class TopicSetup {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    public static void main(String[] args) {
        TopicSetup setup = new TopicSetup();

        try {
            setup.createTopic("demo-topic", 3, (short) 1);
            setup.listTopics();
        } catch (Exception e) {
            System.err.println("Error setting up topics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void createTopic(String topicName, int numPartitions, short replicationFactor)
            throws ExecutionException, InterruptedException {

        System.out.println("Setting up Kafka topic: " + topicName);

        Properties adminProps = new Properties();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

        try (AdminClient adminClient = AdminClient.create(adminProps)) {

            // Check if topic already exists
            ListTopicsResult topics = adminClient.listTopics();
            Set<String> existingTopics = topics.names().get();

            if (existingTopics.contains(topicName)) {
                System.out.println("Topic " + topicName + " already exists");
                return;
            }

            // Create topic with partitions
            NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor);

            // Set topic configuration
            Map<String, String> topicConfigs = new HashMap<>();
            topicConfigs.put(TopicConfig.RETENTION_MS_CONFIG, "86400000"); // 24 hours
            topicConfigs.put(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE);
            topicConfigs.put(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy");
            newTopic.configs(topicConfigs);

            CreateTopicsResult result = adminClient.createTopics(Collections.singleton(newTopic));
            result.all().get();

            System.out.println("Successfully created topic: " + topicName +
                    " with " + numPartitions + " partitions and replication factor " + replicationFactor);

        } catch (Exception e) {
            System.err.println("Error creating topic: " + e.getMessage());
            throw e;
        }
    }

    public void listTopics() throws ExecutionException, InterruptedException {
        Properties adminProps = new Properties();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

        try (AdminClient adminClient = AdminClient.create(adminProps)) {

            ListTopicsResult topics = adminClient.listTopics();
            Set<String> topicNames = topics.names().get();

            System.out.println("\nExisting topics:");
            for (String topicName : topicNames) {
                System.out.println("- " + topicName);
            }

            // Get detailed topic information
            DescribeTopicsResult describeResult = adminClient.describeTopics(topicNames);
            Map<String, TopicDescription> topicDescriptions = describeResult.all().get();

            System.out.println("\nTopic details:");
            for (Map.Entry<String, TopicDescription> entry : topicDescriptions.entrySet()) {
                TopicDescription description = entry.getValue();
                System.out.println("Topic: " + description.name() +
                        ", Partitions: " + description.partitions().size() +
                        ", Internal: " + description.isInternal());
            }

        } catch (Exception e) {
            System.err.println("Error listing topics: " + e.getMessage());
            throw e;
        }
    }

    public void deleteTopic(String topicName) throws ExecutionException, InterruptedException {
        Properties adminProps = new Properties();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

        try (AdminClient adminClient = AdminClient.create(adminProps)) {

            DeleteTopicsResult result = adminClient.deleteTopics(Collections.singleton(topicName));
            result.all().get();

            System.out.println("Successfully deleted topic: " + topicName);

        } catch (Exception e) {
            System.err.println("Error deleting topic: " + e.getMessage());
            throw e;
        }
    }
}