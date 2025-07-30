package com.crossborder.hospitalB.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KafkaProducerConfigTest {

    @Test
    public void testKafkaProducerBeansInitialization() {
        KafkaProducerConfig config = new KafkaProducerConfig();

        // Inject dummy value
        String dummyBootstrap = "localhost:9092";
        ReflectionTestUtils.setField(config, "bootstrapServers", dummyBootstrap);

        ProducerFactory<String, String> factory = config.producerFactory();
        assertNotNull(factory, "ProducerFactory should not be null");

        KafkaTemplate<String, String> template = config.kafkaTemplate();
        assertNotNull(template, "KafkaTemplate should not be null");

        // Validate config map contents
        Map<String, Object> props = factory.getConfigurationProperties();
        assertEquals(dummyBootstrap, props.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(StringSerializer.class, props.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertEquals(StringSerializer.class, props.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
    }
}
