package com.crossborder.hospitalB.producer;

import com.crossborder.hospitalB.model.PatientDataRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.Instant;
import java.util.UUID;

@Service
public class PatientRequestKafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(PatientRequestKafkaProducer.class);
    private static final String TOPIC = "patient-data-request";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void sendPatientDataRequest(PatientDataRequest request) {
        try {
            String requestId = generateRequestId();
            String timestamp = Instant.now().toString();

            request.setRequestId(requestId);
            request.setTimestamp(timestamp);

            if ((request.getPatientIds() == null || request.getPatientIds().isEmpty())
                    && request.getPatientId() == null) {
                throw new IllegalArgumentException("At least one patient ID must be provided");
            }

            List<String> targetIds = request.getPatientId() != null
                    ? List.of(request.getPatientId())
                    : request.getPatientIds();

            String json = objectMapper.writeValueAsString(request);

            for (String patientId : targetIds) {
                kafkaTemplate.send(TOPIC, patientId, json);

                System.out.println("Patient data request sent for ID: " + patientId);
                logger.info("Request sent - ID: {}, Patient: {}, Doctor: {}",
                        requestId, patientId, request.getDoctorId());
            }

        } catch (Exception e) {
            logger.error("Failed to send request for doctor {}: {}",
                    request.getDoctorId(), e.getMessage(), e);
            throw new RuntimeException("Kafka publish failed", e);
        }
    }

    private String generateRequestId() {
        return "REQ-" +
                Instant.now().toEpochMilli() + "-" +
                UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
