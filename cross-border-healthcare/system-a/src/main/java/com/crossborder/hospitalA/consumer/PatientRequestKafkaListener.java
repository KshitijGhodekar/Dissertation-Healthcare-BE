package com.crossborder.hospitalA.consumer;

import com.crossborder.hospitalA.model.PatientDataRequest;
import com.crossborder.hospitalA.service.PatientRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PatientRequestKafkaListener {

    @Autowired
    private PatientRequestService handlerService;

    @KafkaListener(topics = "patient-data-request", groupId = "hospitalA-group")
    public void handleRequest(PatientDataRequest request) {
        try {
            System.out.printf("📥 Received Kafka request: %s%n", request);
            handlerService.processRequest(request);
        } catch (Exception e) {
            System.err.printf("❌ Failed to process Kafka request: %s%n", e.getMessage());
            e.printStackTrace();
        }
    }
}
