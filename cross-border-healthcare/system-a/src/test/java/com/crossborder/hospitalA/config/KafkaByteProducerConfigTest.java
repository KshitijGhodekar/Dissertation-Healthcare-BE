package com.crossborder.hospitalA.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KafkaByteProducerConfigTest {

    @Test
    public void binaryProducerFactoryShouldNotBeNull() {
        KafkaByteProducerConfig config = new KafkaByteProducerConfig();
        ProducerFactory<String, byte[]> factory = config.binaryProducerFactory();
        assertNotNull(factory, "ProducerFactory should not be null");

        Map<String, Object> props = ((DefaultKafkaProducerFactory<String, byte[]>) factory).getConfigurationProperties();
        assertEquals("kafka:9092", props.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(StringSerializer.class, props.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertEquals(ByteArraySerializer.class, props.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
    }

    @Test
    public void binaryKafkaTemplateShouldNotBeNull() {
        KafkaByteProducerConfig config = new KafkaByteProducerConfig();
        KafkaTemplate<String, byte[]> template = config.binaryKafkaTemplate();
        assertNotNull(template, "KafkaTemplate should not be null");
    }
}
