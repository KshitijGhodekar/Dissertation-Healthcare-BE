package com.crossborder.hospitalA.consumer;

import com.crossborder.hospitalA.model.PatientDataRequest;
import com.crossborder.hospitalA.service.PatientRequestService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class PatientRequestKafkaListener {

    private static final Logger logger = LoggerFactory.getLogger(PatientRequestKafkaListener.class);

    @Autowired
    private PatientRequestService handlerService;

    @KafkaListener(topics = "patient-data-request", groupId = "hospitalA-group")
    public void handleRequest(ConsumerRecord<String, PatientDataRequest> record, Acknowledgment ack) {
        try {
            PatientDataRequest request = record.value();
             logger.debug("Received Kafka request for patientId: {}", request.getPatientId());

            handlerService.processRequest(request);
            ack.acknowledge();

        } catch (Exception e) {
            logger.error("Failed to process Kafka request", e);
        }
    }
}
