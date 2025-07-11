package com.crossborder.hospitalA.config;

import com.crossborder.hospitalA.model.PatientDataRequest;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, PatientDataRequest> consumerFactory() {
        JsonDeserializer<PatientDataRequest> deserializer = new JsonDeserializer<>(PatientDataRequest.class, false);
        deserializer.addTrustedPackages("com.crossborder.hospitalA.model");

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                System.getenv().getOrDefault("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:9092"));

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "hospitalA-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PatientDataRequest> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PatientDataRequest> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
