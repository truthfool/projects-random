package com.example.rabbitmq;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeoutException;

public class RabbitMQProducerDemo {

    private static final String HOST = "localhost";
    private static final int PORT = 5672;
    private static final String USERNAME = "guest";
    private static final String PASSWORD = "guest";
    private static final String QUEUE_NAME = "demo-queue";
    private static final String EXCHANGE_NAME = "demo-exchange";
    private static final String ROUTING_KEY = "demo.routing.key";

    public static void main(String[] args) {
        RabbitMQProducerDemo producer = new RabbitMQProducerDemo();
        producer.startProducer();
    }

    public void startProducer() {
        System.out.println("Starting RabbitMQ Producer Demo...");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);

        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {

            // Enable publisher confirms
            channel.confirmSelect();

            for (int i = 1; i <= 20; i++) {
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

                Thread.sleep(500); // Send message every 500ms
            }

            System.out.println("Producer finished sending messages");

        } catch (Exception e) {
            System.err.println("Error in producer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}