package com.crossborder.hospitalB.producer;

import com.crossborder.hospitalB.model.PatientDataRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PatientRequestKafkaProducer {

    private static final String TOPIC = "patient-data-request";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void sendPatientDataRequest(PatientDataRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            kafkaTemplate.send(TOPIC, request.getPatientId(), json);

            System.out.println("📤 Patient data request sent for ID: " + request.getPatientId());
        } catch (Exception e) {
            System.err.println("❌ Kafka send error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
