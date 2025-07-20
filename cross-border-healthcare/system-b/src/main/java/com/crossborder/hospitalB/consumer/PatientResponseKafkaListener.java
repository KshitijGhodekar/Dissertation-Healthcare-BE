package com.crossborder.hospitalB.consumer;

import com.crossborder.hospitalB.service.PatientResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PatientResponseKafkaListener {

    @Autowired
    private PatientResponseService consumerService;

    @KafkaListener(
            topics = "${hospital.kafka.topic}",
            groupId = "hospitalB-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(byte[] message) {
        System.out.println("Received encrypted patient data");
        consumerService.processPatientData(message);
    }
}
