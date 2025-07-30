package com.crossborder.hospitalB.consumer;

import com.crossborder.hospitalB.service.PatientResponseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class PatientResponseKafkaListenerTest {

    private PatientResponseKafkaListener listener;
    private PatientResponseService mockService;

    @BeforeEach
    public void setUp() {
        mockService = Mockito.mock(PatientResponseService.class);
        listener = new PatientResponseKafkaListener();
        // Manually inject mock into listener
        java.lang.reflect.Field serviceField;
        try {
            serviceField = PatientResponseKafkaListener.class.getDeclaredField("consumerService");
            serviceField.setAccessible(true);
            serviceField.set(listener, mockService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock service", e);
        }
    }

    @Test
    public void testListenCallsProcessPatientData() {
        byte[] dummyMessage = "encrypted-data".getBytes();

        listener.listen(dummyMessage);

        Mockito.verify(mockService, Mockito.times(1)).processPatientData(dummyMessage);
    }
}
