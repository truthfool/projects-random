package com.example.rabbitmq;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class QueueSetup {

    private static final String HOST = "localhost";
    private static final int PORT = 5672;
    private static final String USERNAME = "guest";
    private static final String PASSWORD = "guest";

    public static void main(String[] args) {
        QueueSetup setup = new QueueSetup();

        try {
            setup.setupDemoQueue();
            setup.listQueues();
        } catch (Exception e) {
            System.err.println("Error setting up queues: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setupDemoQueue() throws IOException, TimeoutException {
        System.out.println("Setting up RabbitMQ demo queue and exchange...");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);

        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {

            // Declare exchange
            channel.exchangeDeclare("demo-exchange", BuiltinExchangeType.DIRECT, true, false, null);
            System.out.println("Exchange declared: demo-exchange");

            // Declare queue
            channel.queueDeclare("demo-queue", true, false, false, null);
            System.out.println("Queue declared: demo-queue");

            // Bind queue to exchange
            channel.queueBind("demo-queue", "demo-exchange", "demo.routing.key");
            System.out.println("Queue bound to exchange with routing key: demo.routing.key");

            // Create additional queues for different patterns
            setupFanoutExchange(channel);
            setupTopicExchange(channel);

        } catch (Exception e) {
            System.err.println("Error setting up queue: " + e.getMessage());
            throw e;
        }
    }

    private void setupFanoutExchange(Channel channel) throws IOException {
        // Fanout exchange for broadcast messages
        channel.exchangeDeclare("fanout-exchange", BuiltinExchangeType.FANOUT, true, false, null);
        System.out.println("Fanout exchange declared: fanout-exchange");

        // Create queues for fanout
        channel.queueDeclare("fanout-queue-1", true, false, false, null);
        channel.queueDeclare("fanout-queue-2", true, false, false, null);

        // Bind queues to fanout exchange (no routing key needed)
        channel.queueBind("fanout-queue-1", "fanout-exchange", "");
        channel.queueBind("fanout-queue-2", "fanout-exchange", "");

        System.out.println("Fanout queues created and bound");
    }

    private void setupTopicExchange(Channel channel) throws IOException {
        // Topic exchange for pattern-based routing
        channel.exchangeDeclare("topic-exchange", BuiltinExchangeType.TOPIC, true, false, null);
        System.out.println("Topic exchange declared: topic-exchange");

        // Create queues for topic routing
        channel.queueDeclare("topic-queue-orders", true, false, false, null);
        channel.queueDeclare("topic-queue-notifications", true, false, false, null);
        channel.queueDeclare("topic-queue-all", true, false, false, null);

        // Bind queues with different routing patterns
        channel.queueBind("topic-queue-orders", "topic-exchange", "orders.*");
        channel.queueBind("topic-queue-notifications", "topic-exchange", "*.notification");
        channel.queueBind("topic-queue-all", "topic-exchange", "#");

        System.out.println("Topic queues created and bound with patterns");
    }

    public void listQueues() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);

        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {

            // Note: RabbitMQ Java client doesn't have direct API to list queues
            // This would typically be done via management API or command line
            System.out.println("\nQueues and exchanges have been created.");
            System.out.println("You can view them in RabbitMQ Management UI at http://localhost:15672");
            System.out.println("Username: guest, Password: guest");

        } catch (Exception e) {
            System.err.println("Error listing queues: " + e.getMessage());
            throw e;
        }
    }

    public void deleteQueue(String queueName) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);

        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {

            channel.queueDelete(queueName);
            System.out.println("Successfully deleted queue: " + queueName);

        } catch (Exception e) {
            System.err.println("Error deleting queue: " + e.getMessage());
            throw e;
        }
    }

    public void deleteExchange(String exchangeName) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);

        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {

            channel.exchangeDelete(exchangeName);
            System.out.println("Successfully deleted exchange: " + exchangeName);

        } catch (Exception e) {
            System.err.println("Error deleting exchange: " + e.getMessage());
            throw e;
        }
    }
}