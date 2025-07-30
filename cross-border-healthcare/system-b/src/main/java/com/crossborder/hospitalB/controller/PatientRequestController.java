package com.crossborder.hospitalB.controller;

import com.crossborder.hospitalB.model.PatientDataRequest;
import com.crossborder.hospitalB.model.PatientEntity;
import com.crossborder.hospitalB.producer.PatientRequestKafkaProducer;
import com.crossborder.hospitalB.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@RestController
@RequestMapping("/api/request")
public class PatientRequestController {

    private static final Logger logger = LoggerFactory.getLogger(PatientRequestController.class);

    @Autowired
    private PatientRequestKafkaProducer producerService;

    @Autowired
    private PatientRepository patientRepository;

    @PostMapping
    public ResponseEntity<String> requestPatientData(@RequestBody PatientDataRequest request) {
        try {
            producerService.sendPatientDataRequest(request);
            return ResponseEntity.ok("Request sent successfully.");
        } catch (Exception e) {
            logger.error("Failed to send patient data request for doctorId={} and patientId={}: {}",
                    request.getDoctorId(), request.getPatientId(), e.getMessage(), e);
            return ResponseEntity
                    .status(500)
                    .body("Failed to send request: " + e.getMessage());
        }
    }

    @GetMapping("/records")
    public ResponseEntity<List<PatientEntity>> getAllPatientRecords() {
        return ResponseEntity.ok(patientRepository.findAll());
    }

    @GetMapping(value = "/records/{patientId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Transactional
    public ResponseEntity<byte[]> downloadPatientPdf(@PathVariable String patientId) {
        return patientRepository.findByPatientId(patientId)
                .filter(patient -> patient.getPdfReport() != null)
                .map(patient -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + patientId + ".pdf")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(patient.getPdfReport()))
                .orElse(ResponseEntity.notFound().build());
    }

}
