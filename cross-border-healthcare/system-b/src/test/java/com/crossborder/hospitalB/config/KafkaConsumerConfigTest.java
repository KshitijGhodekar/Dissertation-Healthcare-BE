package com.crossborder.hospitalB.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KafkaConsumerConfigTest {

    @Test
    public void testKafkaBeansInitialization() {
        KafkaConsumerConfig config = new KafkaConsumerConfig();

        // Inject dummy Kafka config values using reflection
        String dummyBootstrap = "localhost:9092";
        String dummyGroupId = "test-group";

        ReflectionTestUtils.setField(config, "bootstrapServers", dummyBootstrap);
        ReflectionTestUtils.setField(config, "groupId", dummyGroupId);

        ConsumerFactory<String, byte[]> factory = config.binaryConsumerFactory();
        assertNotNull(factory, "ConsumerFactory should not be null");

        ConcurrentKafkaListenerContainerFactory<String, byte[]> listenerFactory = config.kafkaListenerContainerFactory();
        assertNotNull(listenerFactory, "KafkaListenerContainerFactory should not be null");

        Map<String, Object> props = factory.getConfigurationProperties();
        assertEquals(dummyBootstrap, props.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(dummyGroupId, props.get(ConsumerConfig.GROUP_ID_CONFIG));
        assertEquals(StringDeserializer.class, props.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
    }
}
