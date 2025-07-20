package com.example.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Date;

public class KafkaProducerDemo {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC_NAME = "demo-topic";

    public static void main(String[] args) {
        KafkaProducerDemo producer = new KafkaProducerDemo();
        producer.startProducer();
    }

    public void startProducer() {
        System.out.println("Starting Kafka Producer Demo...");

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

            for (int i = 1; i <= 20; i++) {
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

                Thread.sleep(500); // Send message every 500ms
            }

            producer.flush();
            System.out.println("Producer finished sending messages");

        } catch (Exception e) {
            System.err.println("Error in producer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}