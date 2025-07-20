package com.crossborder.hospitalB.service;

import com.crossborder.hospitalB.encryption.AESEncryptionUtil;
import com.crossborder.hospitalB.model.PatientEntity;
import com.crossborder.hospitalB.repository.PatientRepository;
import com.fhir.validator.FHIRValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class PatientResponseService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private EmailNotificationService emailService;

    private final ObjectMapper mapper = new ObjectMapper();

    public void processPatientData(byte[] encryptedData) {
        try {
            // Step 1: Decrypt
            String decryptedJson = AESEncryptionUtil.decrypt(encryptedData);
            System.out.println("Decrypted JSON:\n" + decryptedJson);

            // Step 2: Parse JSON
            JsonNode json = mapper.readTree(decryptedJson);

            // Step 3: FHIR Validation
            FHIRValidator.validatePatient(new JSONObject(decryptedJson));

            // Step 4: Map to JPA entity
            PatientEntity patient = new PatientEntity();
            patient.setPatientId(json.path("patientId").asText());
            patient.setName(json.path("name").asText());
            patient.setAge(json.path("age").asInt());
            patient.setGender(json.path("gender").asText());
            patient.setBloodType(json.path("bloodType").asText());
            patient.setMedicalCondition(json.path("medicalCondition").asText());
            patient.setDateOfAdmission(LocalDate.parse(json.path("dateOfAdmission").asText()));
            patient.setDischargeDate(LocalDate.parse(json.path("dischargeDate").asText()));
            patient.setDoctor(json.path("doctor").asText()); // doctor from System A DB
            patient.setHospital(json.path("hospital").asText());
            patient.setMedication(json.path("medication").asText());
            patient.setTestResults(json.path("testResults").asText());

            // Step 5: Save patient
            patientRepository.save(patient);
            System.out.println("Patient saved: " + patient.getPatientId());

            // Step 6: Get doctorName from JSON
            String doctorName = json.path("doctorName").asText(); // ✅ name from request

            // Step 7: Send email
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
