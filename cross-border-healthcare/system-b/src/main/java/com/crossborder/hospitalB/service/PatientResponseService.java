package com.crossborder.hospitalB.service;

import com.crossborder.hospitalB.encryption.AESEncryptionUtil;
import com.crossborder.hospitalB.model.PatientEntity;
import com.crossborder.hospitalB.repository.PatientRepository;
import com.fhir.validator.FHIRValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientResponseService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private EmailNotificationService emailService;

    private final ObjectMapper mapper = new ObjectMapper();

    public void processPatientData(byte[] encryptedData) {
        try {
            /* 1️⃣  Decrypt */
            String decryptedJson = AESEncryptionUtil.decrypt(encryptedData);
            /* 2️⃣  Parse full JSON tree */
            JsonNode fullJson = mapper.readTree(decryptedJson);

            /* 3️⃣  Build minimal JSON for validation */
            ObjectNode validatable = mapper.createObjectNode();
            validatable.put("patientId", fullJson.path("patientId").asText(""));
            validatable.put("name",      fullJson.path("name").asText(""));
            validatable.put("diagnosis", fullJson.path("diagnosis").asText(""));

            System.out.println("🧪 Will validate:\n" + validatable.toPrettyString());

            /* 4️⃣  Validate against schema */
            FHIRValidator.validatePatient(new JSONObject(validatable.toString()));

            /* 5️⃣  Persist full entity */
            PatientEntity patient = mapper.treeToValue(fullJson, PatientEntity.class);
            patientRepository.save(patient);
            System.out.println("✅ Patient data saved for: " + patient.getPatientId());

            emailService.sendAccessAlert(
                    "kshitijdghodekar@gmail.com",          // replace with actual doctor email
                    patient.getDoctorId(),
                    patient.getPatientId(),
                    true // access granted
            );

        } catch (Exception e) {
            System.err.println("❌ Error in processPatientData: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
