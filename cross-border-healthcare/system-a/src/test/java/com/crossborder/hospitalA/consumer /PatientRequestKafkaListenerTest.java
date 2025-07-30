package com.crossborder.hospitalA.consumer;

import com.crossborder.hospitalA.model.PatientDataRequest;
import com.crossborder.hospitalA.service.PatientRequestService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

public class PatientRequestKafkaListenerTest {

    private PatientRequestKafkaListener listener;
    private PatientRequestService handlerService;

    @BeforeEach
    void setUp() {
        handlerService = mock(PatientRequestService.class);
        listener = new PatientRequestKafkaListener();

        try {
            Field field = PatientRequestKafkaListener.class.getDeclaredField("handlerService");
            field.setAccessible(true);
            field.set(listener, handlerService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock PatientRequestService", e);
        }
    }

    @Test
    void testHandleRequest_withValidRecord_callsServiceAndAcknowledges() {
        PatientDataRequest mockRequest = new PatientDataRequest();
        mockRequest.setDoctorId("doc123");
        mockRequest.setPatientId("pat001");

        ConsumerRecord<String, PatientDataRequest> record = new ConsumerRecord<>(
                "patient-data-request", 0, 0L, "key", mockRequest
        );

        Acknowledgment ack = mock(Acknowledgment.class);

        listener.handleRequest(record, ack);

        verify(handlerService, times(1)).processRequest(mockRequest);
        verify(ack, times(1)).acknowledge();
    }

    @Test
    void testHandleRequest_whenExceptionThrown_doesNotAcknowledge() {
        PatientDataRequest mockRequest = new PatientDataRequest();
        mockRequest.setDoctorId("doc999");
        mockRequest.setPatientId("pat999");

        ConsumerRecord<String, PatientDataRequest> record = new ConsumerRecord<>(
                "patient-data-request", 0, 0L, "key", mockRequest
        );

        Acknowledgment ack = mock(Acknowledgment.class);

        doThrow(new RuntimeException("Simulated failure"))
                .when(handlerService).processRequest(any(PatientDataRequest.class));

        listener.handleRequest(record, ack);

        verify(handlerService, times(1)).processRequest(mockRequest);
        verify(ack, never()).acknowledge(); // shouldn't acknowledge on failure
    }
}
