package com.crossborder.hospitalB.controller;

import com.crossborder.hospitalB.model.PatientDataRequest;
import com.crossborder.hospitalB.producer.PatientRequestKafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/request")
public class PatientRequestController {

    @Autowired
    private PatientRequestKafkaProducer producerService;

    @PostMapping
    public ResponseEntity<String> requestPatientData(@RequestBody PatientDataRequest request) {
        try {
            producerService.sendPatientDataRequest(request);
            return ResponseEntity.ok("✅ Request sent successfully.");
        } catch (Exception e) {
            // Log for debugging (optional)
            e.printStackTrace();
            return ResponseEntity
                    .status(500)
                    .body("❌ Failed to send request: " + e.getMessage());
        }
    }
}
