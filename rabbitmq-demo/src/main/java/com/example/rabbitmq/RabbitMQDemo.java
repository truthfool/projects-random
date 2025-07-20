package com.example.rabbitmq;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RabbitMQDemo {

    private static final String HOST = "localhost";
    private static final int PORT = 5672;
    private static final String USERNAME = "guest";
    private static final String PASSWORD = "guest";
    private static final String QUEUE_NAME = "demo-queue";
    private static final String EXCHANGE_NAME = "demo-exchange";
    private static final String ROUTING_KEY = "demo.routing.key";

    public static void main(String[] args) {
        RabbitMQDemo demo = new RabbitMQDemo();

        try {
            // Setup RabbitMQ queue and exchange
            demo.setupQueue();

            // Run producer and consumer in separate threads
            demo.runDemo();

        } catch (Exception e) {
            System.err.println("Error running RabbitMQ demo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setupQueue() throws IOException, TimeoutException {
        System.out.println("Setting up RabbitMQ queue and exchange...");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);

        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {

            // Declare exchange
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true, false, null);
            System.out.println("Exchange declared: " + EXCHANGE_NAME);

            // Declare queue
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            System.out.println("Queue declared: " + QUEUE_NAME);

            // Bind queue to exchange
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
            System.out.println("Queue bound to exchange with routing key: " + ROUTING_KEY);

        } catch (Exception e) {
            System.err.println("Error setting up queue: " + e.getMessage());
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
        System.out.println("Starting RabbitMQ Producer...");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);

        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {

            // Enable publisher confirms
            channel.confirmSelect();

            for (int i = 1; i <= 10; i++) {
                String message = "Message " + i + " from producer at " + new Date();

                // Publish message to exchange
                channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        message.getBytes(StandardCharsets.UTF_8));

                // Wait for confirmation
                if (channel.waitForConfirms(5000)) {
                    System.out.println("Producer: Message " + i + " sent successfully");
                } else {
                    System.err.println("Producer: Message " + i + " failed to send");
                }

                Thread.sleep(1000); // Send message every second
            }

            System.out.println("Producer finished sending messages");

        } catch (Exception e) {
            System.err.println("Error in producer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void runConsumer() {
        System.out.println("Starting RabbitMQ Consumer...");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);

        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {

            // Set QoS (prefetch count)
            channel.basicQos(1);

            // Create consumer
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("Consumer: Received message - " +
                        "exchange=" + delivery.getEnvelope().getExchange() +
                        ", routingKey=" + delivery.getEnvelope().getRoutingKey() +
                        ", deliveryTag=" + delivery.getEnvelope().getDeliveryTag() +
                        ", message=" + message);

                // Acknowledge message
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };

            CancelCallback cancelCallback = consumerTag -> {
                System.err.println("Consumer was cancelled");
            };

            // Start consuming
            String consumerTag = channel.basicConsume(QUEUE_NAME, false, deliverCallback, cancelCallback);
            System.out.println("Consumer started with tag: " + consumerTag);

            // Keep consumer running
            while (true) {
                Thread.sleep(100);
            }

        } catch (Exception e) {
            System.err.println("Error in consumer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}