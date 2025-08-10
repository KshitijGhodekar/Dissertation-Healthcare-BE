package com.crossborder.hospitalB.producer;

import com.crossborder.hospitalB.model.PatientDataRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PatientRequestKafkaProducerTest {

    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper;
    private PatientRequestKafkaProducer producer;

    @BeforeEach
    public void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        objectMapper = mock(ObjectMapper.class);
        producer = new PatientRequestKafkaProducer();

        // Inject mocks manually
        injectField(producer, "kafkaTemplate", kafkaTemplate);
        injectField(producer, "objectMapper", objectMapper);
    }

    private void injectField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSendWithMultiplePatientIds() throws Exception {
        PatientDataRequest request = new PatientDataRequest();
        request.setPatientIds(Arrays.asList("P001", "P002"));
        String jsonPayload = "{\"dummy\":\"json\"}";

        when(objectMapper.writeValueAsString(request)).thenReturn(jsonPayload);

        producer.sendPatientDataRequest(request);

        verify(kafkaTemplate, times(2)).send(eq("patient-data-request"), anyString(), eq(jsonPayload));
    }

    @Test
    public void testSendWithSinglePatientId() throws Exception {
        PatientDataRequest request = new PatientDataRequest();
        request.setPatientId("P123");
        String jsonPayload = "{\"dummy\":\"json\"}";

        when(objectMapper.writeValueAsString(request)).thenReturn(jsonPayload);

        producer.sendPatientDataRequest(request);

        verify(kafkaTemplate, times(1)).send("patient-data-request", "P123", jsonPayload);
    }

    @Test
    public void testSendWithNoPatientIds() throws Exception {
        PatientDataRequest request = new PatientDataRequest();
        String jsonPayload = "{\"dummy\":\"json\"}";

        when(objectMapper.writeValueAsString(request)).thenReturn(jsonPayload);

        // Expect an exception to be thrown
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> producer.sendPatientDataRequest(request));

        // Verify the exception message
        assertEquals("Kafka publish failed", exception.getMessage());

        // Verify that no Kafka send was attempted
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }
}
