package com.example.rabbitmq;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeoutException;

public class AdvancedExchangeDemo {

    private static final String HOST = "localhost";
    private static final int PORT = 5672;
    private static final String USERNAME = "guest";
    private static final String PASSWORD = "guest";

    public static void main(String[] args) {
        AdvancedExchangeDemo demo = new AdvancedExchangeDemo();

        try {
            demo.runAdvancedDemo();
        } catch (Exception e) {
            System.err.println("Error running advanced demo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void runAdvancedDemo() throws IOException, TimeoutException {
        System.out.println("Running Advanced RabbitMQ Exchange Demo...");

        // Setup exchanges and queues
        setupExchanges();

        // Run producers for different exchange types
        runDirectExchangeProducer();
        runFanoutExchangeProducer();
        runTopicExchangeProducer();

        // Run consumers for different queues
        runConsumers();
    }

    private void setupExchanges() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);

        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {

            // Direct Exchange
            channel.exchangeDeclare("direct-exchange", BuiltinExchangeType.DIRECT, true, false, null);
            channel.queueDeclare("direct-queue", true, false, false, null);
            channel.queueBind("direct-queue", "direct-exchange", "direct.key");

            // Fanout Exchange
            channel.exchangeDeclare("fanout-exchange", BuiltinExchangeType.FANOUT, true, false, null);
            channel.queueDeclare("fanout-queue-1", true, false, false, null);
            channel.queueDeclare("fanout-queue-2", true, false, false, null);
            channel.queueBind("fanout-queue-1", "fanout-exchange", "");
            channel.queueBind("fanout-queue-2", "fanout-exchange", "");

            // Topic Exchange
            channel.exchangeDeclare("topic-exchange", BuiltinExchangeType.TOPIC, true, false, null);
            channel.queueDeclare("topic-queue-orders", true, false, false, null);
            channel.queueDeclare("topic-queue-notifications", true, false, false, null);
            channel.queueDeclare("topic-queue-all", true, false, false, null);
            channel.queueBind("topic-queue-orders", "topic-exchange", "orders.*");
            channel.queueBind("topic-queue-notifications", "topic-exchange", "*.notification");
            channel.queueBind("topic-queue-all", "topic-exchange", "#");

            System.out.println("All exchanges and queues setup complete");

        }
    }

    private void runDirectExchangeProducer() {
        new Thread(() -> {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(HOST);
                factory.setPort(PORT);
                factory.setUsername(USERNAME);
                factory.setPassword(PASSWORD);

                try (Connection connection = factory.newConnection();
                        Channel channel = connection.createChannel()) {

                    channel.confirmSelect();

                    for (int i = 1; i <= 5; i++) {
                        String message = "Direct message " + i + " at " + new Date();
                        channel.basicPublish("direct-exchange", "direct.key",
                                MessageProperties.PERSISTENT_TEXT_PLAIN,
                                message.getBytes(StandardCharsets.UTF_8));

                        if (channel.waitForConfirms(1000)) {
                            System.out.println("Direct Producer: Message " + i + " sent");
                        }

                        Thread.sleep(500);
                    }
                }
            } catch (Exception e) {
                System.err.println("Direct producer error: " + e.getMessage());
            }
        }).start();
    }

    private void runFanoutExchangeProducer() {
        new Thread(() -> {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(HOST);
                factory.setPort(PORT);
                factory.setUsername(USERNAME);
                factory.setPassword(PASSWORD);

                try (Connection connection = factory.newConnection();
                        Channel channel = connection.createChannel()) {

                    channel.confirmSelect();

                    for (int i = 1; i <= 3; i++) {
                        String message = "Fanout broadcast message " + i + " at " + new Date();
                        channel.basicPublish("fanout-exchange", "",
                                MessageProperties.PERSISTENT_TEXT_PLAIN,
                                message.getBytes(StandardCharsets.UTF_8));

                        if (channel.waitForConfirms(1000)) {
                            System.out.println("Fanout Producer: Broadcast message " + i + " sent");
                        }

                        Thread.sleep(1000);
                    }
                }
            } catch (Exception e) {
                System.err.println("Fanout producer error: " + e.getMessage());
            }
        }).start();
    }

    private void runTopicExchangeProducer() {
        new Thread(() -> {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(HOST);
                factory.setPort(PORT);
                factory.setUsername(USERNAME);
                factory.setPassword(PASSWORD);

                try (Connection connection = factory.newConnection();
                        Channel channel = connection.createChannel()) {

                    channel.confirmSelect();

                    // Send order messages
                    for (int i = 1; i <= 3; i++) {
                        String message = "Order message " + i + " at " + new Date();
                        channel.basicPublish("topic-exchange", "orders.new",
                                MessageProperties.PERSISTENT_TEXT_PLAIN,
                                message.getBytes(StandardCharsets.UTF_8));

                        if (channel.waitForConfirms(1000)) {
                            System.out.println("Topic Producer: Order message " + i + " sent");
                        }

                        Thread.sleep(500);
                    }

                    // Send notification messages
                    for (int i = 1; i <= 3; i++) {
                        String message = "Notification message " + i + " at " + new Date();
                        channel.basicPublish("topic-exchange", "system.notification",
                                MessageProperties.PERSISTENT_TEXT_PLAIN,
                                message.getBytes(StandardCharsets.UTF_8));

                        if (channel.waitForConfirms(1000)) {
                            System.out.println("Topic Producer: Notification message " + i + " sent");
                        }

                        Thread.sleep(500);
                    }

                    // Send general messages
                    for (int i = 1; i <= 2; i++) {
                        String message = "General message " + i + " at " + new Date();
                        channel.basicPublish("topic-exchange", "general.info",
                                MessageProperties.PERSISTENT_TEXT_PLAIN,
                                message.getBytes(StandardCharsets.UTF_8));

                        if (channel.waitForConfirms(1000)) {
                            System.out.println("Topic Producer: General message " + i + " sent");
                        }

                        Thread.sleep(500);
                    }
                }
            } catch (Exception e) {
                System.err.println("Topic producer error: " + e.getMessage());
            }
        }).start();
    }

    private void runConsumers() {
        // Start consumers for different queues
        startConsumer("Direct Consumer", "direct-queue");
        startConsumer("Fanout Consumer 1", "fanout-queue-1");
        startConsumer("Fanout Consumer 2", "fanout-queue-2");
        startConsumer("Topic Orders Consumer", "topic-queue-orders");
        startConsumer("Topic Notifications Consumer", "topic-queue-notifications");
        startConsumer("Topic All Consumer", "topic-queue-all");
    }

    private void startConsumer(String consumerName, String queueName) {
        new Thread(() -> {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(HOST);
                factory.setPort(PORT);
                factory.setUsername(USERNAME);
                factory.setPassword(PASSWORD);

                try (Connection connection = factory.newConnection();
                        Channel channel = connection.createChannel()) {

                    channel.basicQos(1);

                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                        System.out.println(consumerName + ": Received - " + message);
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    };

                    CancelCallback cancelCallback = consumerTag -> {
                        System.err.println(consumerName + " was cancelled");
                    };

                    String consumerTag = channel.basicConsume(queueName, false, deliverCallback, cancelCallback);
                    System.out.println(consumerName + " started with tag: " + consumerTag);

                    // Keep consumer running
                    while (true) {
                        Thread.sleep(100);
                    }

                }
            } catch (Exception e) {
                System.err.println(consumerName + " error: " + e.getMessage());
            }
        }).start();
    }
}