package com.crossborder.hospitalB.service;

import com.crossborder.hospitalB.encryption.AESEncryptionUtil;
import com.crossborder.hospitalB.encryption.ECDSAUtil;
import com.crossborder.hospitalB.model.PatientEntity;
import com.crossborder.hospitalB.repository.PatientRepository;
import com.fhir.validator.FHIRValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;

@Service
public class PatientResponseService {

    private static final Logger logger = LoggerFactory.getLogger(PatientResponseService.class);

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private EmailNotificationService emailService;

    private final ObjectMapper mapper = new ObjectMapper();

    public void processPatientData(byte[] messageBytes) {
        try {
            // Step 1: Parse incoming Kafka JSON payload
            String message = new String(messageBytes, StandardCharsets.UTF_8);
            JsonNode root = mapper.readTree(message);

            String encryptedBase64 = root.get("encryptedData").asText();
            String signature = root.get("signature").asText();
            logger.info("Received encrypted patient data. Starting verification...");

            // Step 2: Verify ECDSA signature
            if (!ECDSAUtil.verify(encryptedBase64, signature)) {
                logger.warn("ECDSA verification failed. Possible tampering detected.");
                return;
            }
            logger.info("ECDSA signature verification successful.");

            // Step 3: Decrypt AES payload
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);
            String decryptedJson = AESEncryptionUtil.decrypt(encryptedBytes);
            logger.info("Patient data decrypted successfully.");

            // Step 4: Parse decrypted JSON
            JsonNode json = mapper.readTree(decryptedJson);

            // Step 5: FHIR validation
            logger.info("Starting FHIR validation for patient data...");
            FHIRValidator.validatePatient(new JSONObject(decryptedJson));
            logger.info("FHIR validation successful.");

            // Step 6: Map to entity
            PatientEntity patient = new PatientEntity();
            patient.setRequestId(json.path("requestId").asText());
            patient.setPatientId(json.path("patientId").asText());
            patient.setName(json.path("name").asText());
            patient.setAge(json.path("age").asInt());
            patient.setGender(json.path("gender").asText());
            patient.setBloodType(json.path("bloodType").asText());
            patient.setMedicalCondition(json.path("medicalCondition").asText());

            if (!json.path("dateOfAdmission").isMissingNode()) {
                patient.setDateOfAdmission(LocalDate.parse(json.path("dateOfAdmission").asText()));
            }
            if (!json.path("dischargeDate").isMissingNode()) {
                patient.setDischargeDate(LocalDate.parse(json.path("dischargeDate").asText()));
            }

            patient.setDoctor(json.path("doctor").asText());
            patient.setHospital(json.path("hospital").asText());
            patient.setMedication(json.path("medication").asText());
            patient.setTestResults(json.path("testResults").asText());

            // Step 7: Save to DB first (without PDF)
            patientRepository.save(patient);
            logger.info("Patient saved successfully with ID: {}", patient.getPatientId());

            // Step 8: Generate PDF and send email
            String doctorName = json.path("doctorName").asText();
            byte[] pdfBytes = emailService.sendPatientDataPDF(
                    "kshitijdghodekar@gmail.com",
                    doctorName,
                    patient,
                    true
            );

            if (pdfBytes != null) {
                patient.setPdfReport(pdfBytes);
                patientRepository.save(patient);
                logger.info("PDF stored in DB for patient ID: {}", patient.getPatientId());
            }

        } catch (Exception e) {
            logger.error("Error in processPatientData: {}", e.getMessage(), e);
        }
    }
}
