package com.crossborder.hospitalA.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import com.crossborder.hospitalA.fabric.FabricClient;

import java.util.*;

@RestController
@RequestMapping("/api/system-status")
public class SystemStatusController {

    @Autowired
    private KafkaTemplate<String, byte[]> binaryKafkaTemplate;

    @Autowired
    private FabricClient fabricClient;

    @GetMapping
    public Map<String, Boolean> getSystemStatus() {
        Map<String, Boolean> status = new HashMap<>();

        // Kafka status check
        try {
            binaryKafkaTemplate.partitionsFor("patient-data-request");
            status.put("kafka", true);
        } catch (Exception e) {
            status.put("kafka", false);
        }

        // Fabric status check using new health method
        status.put("fabric", fabricClient.isFabricAlive());

        return status;
    }
}
