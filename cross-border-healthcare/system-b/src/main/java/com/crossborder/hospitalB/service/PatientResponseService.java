package com.crossborder.hospitalB.service;

import com.crossborder.hospitalB.encryption.AESEncryptionUtil;
import com.crossborder.hospitalB.encryption.ECDSAUtil;
import com.crossborder.hospitalB.model.PatientEntity;
import com.crossborder.hospitalB.repository.PatientRepository;
import com.fhir.validator.FHIRValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;

@Service
public class PatientResponseService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private EmailNotificationService emailService;

    private final ObjectMapper mapper = new ObjectMapper();

    public void processPatientData(byte[] messageBytes) {
        try {
            // 1: Parse incoming JSON Kafka payload
            String message = new String(messageBytes, StandardCharsets.UTF_8);
            JsonNode root = mapper.readTree(message);

            String encryptedBase64 = root.get("encryptedData").asText();
            String signature = root.get("signature").asText();

            // 2: Verify ECDSA signature
            boolean verified = ECDSAUtil.verify(encryptedBase64, signature);
            if (!verified) {
                System.err.println("ECDSA verification failed! Possible tampering.");
                return;
            }

            System.out.println("ECDSA verification successful.");

            // 3: Decrypt the AES payload
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);
            String decryptedJson = AESEncryptionUtil.decrypt(encryptedBytes);
            System.out.println("Decrypted JSON:\n" + decryptedJson);

            // 4: Parse decrypted JSON
            JsonNode json = mapper.readTree(decryptedJson);

            // 5: FHIR validation
            FHIRValidator.validatePatient(new JSONObject(decryptedJson));

            // Step 6: Map to entity
            PatientEntity patient = new PatientEntity();
            patient.setPatientId(json.path("patientId").asText());
            patient.setName(json.path("name").asText());
            patient.setAge(json.path("age").asInt());
            patient.setGender(json.path("gender").asText());
            patient.setBloodType(json.path("bloodType").asText());
            patient.setMedicalCondition(json.path("medicalCondition").asText());
            patient.setDateOfAdmission(LocalDate.parse(json.path("dateOfAdmission").asText()));
            patient.setDischargeDate(LocalDate.parse(json.path("dischargeDate").asText()));
            patient.setDoctor(json.path("doctor").asText());
            patient.setHospital(json.path("hospital").asText());
            patient.setMedication(json.path("medication").asText());
            patient.setTestResults(json.path("testResults").asText());

            // Step 7: Save to DB
            patientRepository.save(patient);
            System.out.println("Patient saved: " + patient.getPatientId());

            // Step 8: Send email
            String doctorName = json.path("doctorName").asText();
            emailService.sendPatientDataPDF(
                    "kshitijdghodekar@gmail.com",
                    doctorName,
                    patient,
                    true
            );

        } catch (Exception e) {
            System.err.println("Error in processPatientData: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
