package com.crossborder.hospitalA.config;

import com.crossborder.hospitalA.model.PatientDataRequest;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KafkaConsumerConfigTest {

    @Test
    public void testConsumerFactoryAndListenerFactory() {
        // Set env property substitute manually
        System.setProperty("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:9092");

        KafkaConsumerConfig config = new KafkaConsumerConfig();
        ConsumerFactory<String, PatientDataRequest> factory = config.consumerFactory();
        assertNotNull(factory, "ConsumerFactory should not be null");

        Map<String, Object> props = factory.getConfigurationProperties();
        assertEquals("kafka:9092", props.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(StringDeserializer.class, props.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));

        ConcurrentKafkaListenerContainerFactory<String, PatientDataRequest> listenerFactory =
                config.kafkaListenerContainerFactory();
        assertNotNull(listenerFactory, "KafkaListenerContainerFactory should not be null");
    }
}
