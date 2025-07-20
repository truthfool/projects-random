package com.example.rabbitmq;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class RabbitMQConsumerDemo {

    private static final String HOST = "localhost";
    private static final int PORT = 5672;
    private static final String USERNAME = "guest";
    private static final String PASSWORD = "guest";
    private static final String QUEUE_NAME = "demo-queue";

    public static void main(String[] args) {
        RabbitMQConsumerDemo consumer = new RabbitMQConsumerDemo();
        consumer.startConsumer();
    }

    public void startConsumer() {
        System.out.println("Starting RabbitMQ Consumer Demo...");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);

        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {

            // Set QoS (prefetch count)
            channel.basicQos(1);

            System.out.println("Consumer waiting for messages... (Press Ctrl+C to stop)");

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