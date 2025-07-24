package com.crossborder.hospitalA.service;

import com.crossborder.hospitalA.encryption.AESEncryptionUtil;
import com.crossborder.hospitalA.encryption.ECDSAUtil;
import com.crossborder.hospitalA.fabric.FabricClient;
import com.crossborder.hospitalA.model.*;
import com.crossborder.hospitalA.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class PatientRequestService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AccessLogRepository accessLogRepository;

    @Autowired
    private FabricLogRepository fabricLogRepository;

    @Autowired
    private KafkaTemplate<String, byte[]> binaryKafkaTemplate;

    public void processRequest(PatientDataRequest request) {
        List<String> patientIds = resolvePatientIds(request);

        if (patientIds.isEmpty()) {
            System.err.println("No valid patient ID(s) found in the request.");
            return;
        }

        for (String pid : patientIds) {
            request.setPatientId(pid);
            processSinglePatient(request);
        }
    }

    private void processSinglePatient(PatientDataRequest request) {
        FabricClient fabricClient = null;

        try {
            System.out.println("Processing patient: " + request.getPatientId());

            // 1. Verify access
            fabricClient = new FabricClient();
            boolean accessGranted = fabricClient.isDoctorAuthorized(
                    request.getDoctorId(),
                    request.getPatientId(),
                    request.getPurpose(),
                    request.getHospitalName()
            );

            // 2. Log access attempt
            AccessLog accessLog = new AccessLog();
            accessLog.setDoctorId(request.getDoctorId());
            accessLog.setDoctorName(request.getDoctorName());
            accessLog.setPatientId(request.getPatientId());
            accessLog.setPurpose(request.getPurpose());
            accessLog.setHospitalName(request.getHospitalName());
            accessLog.setTimestamp(request.getTimestamp());
            accessLog.setAccessGranted(accessGranted);
            accessLogRepository.save(accessLog);

            // 3. Save Fabric log
            FabricLog fabricLog = new FabricLog();
            fabricLog.setDoctorId(request.getDoctorId());
            fabricLog.setDoctorName(request.getDoctorName());
            fabricLog.setPatientId(request.getPatientId());
            fabricLog.setStatus(accessGranted ? "granted" : "denied");
            fabricLog.setTimestamp(request.getTimestamp());
            fabricLogRepository.save(fabricLog);

            if (!accessGranted) {
                System.out.println("Access denied for patient: " + request.getPatientId());
                return;
            }

            // 4. Fetch patient
            PatientEntity patient = patientRepository.findByPatientId(request.getPatientId());
            if (patient == null) {
                System.err.println("Patient not found: " + request.getPatientId());
                return;
            }

            // 5. Convert to JSON (FHIR-style)
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode patientJson = mapper.createObjectNode();
            patientJson.put("patientId", patient.getPatientId());
            patientJson.put("name", patient.getName());
            patientJson.put("doctorName", request.getDoctorName());
            patientJson.put("age", patient.getAge());
            patientJson.put("gender", patient.getGender());
            patientJson.put("bloodType", patient.getBloodType());
            patientJson.put("medicalCondition", patient.getMedicalCondition());
            patientJson.put("dateOfAdmission", patient.getDateOfAdmission().toString());
            patientJson.put("dischargeDate", patient.getDischargeDate().toString());
            patientJson.put("doctor", patient.getDoctor());
            patientJson.put("hospital", patient.getHospital());
            patientJson.put("medication", patient.getMedication());
            patientJson.put("testResults", patient.getTestResults());

            String json = mapper.writeValueAsString(patientJson);
            System.out.println("Enriched Patient JSON:\n" + json);

            // 6. Encrypt
            byte[] encrypted = AESEncryptionUtil.encryptToBytes(json);
            String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);

            // 7. Sign with ECDSA
            String signature = ECDSAUtil.sign(encryptedBase64);

            // 8. Build Kafka message
            ObjectNode signedPayload = mapper.createObjectNode();
            signedPayload.put("encryptedData", encryptedBase64);
            signedPayload.put("signature", signature);

            byte[] kafkaPayload = signedPayload.toString().getBytes(StandardCharsets.UTF_8);
            String topic = resolveTopic(request.getHospitalName());

            binaryKafkaTemplate.send(topic, request.getPatientId(), kafkaPayload);
            System.out.println("Message sent to Kafka topic: " + topic);

        } catch (Exception e) {
            System.err.println("Exception while processing: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (fabricClient != null) {
                fabricClient.close();
            }
        }
    }

    private List<String> resolvePatientIds(PatientDataRequest request) {
        if (request.getPatientIds() != null && !request.getPatientIds().isEmpty()) {
            return request.getPatientIds();
        } else if (request.getPatientId() != null) {
            return List.of(request.getPatientId());
        } else if (request.getMobileNumber() != null) {
            PatientEntity patient = patientRepository.findByMobileNumber(request.getMobileNumber());
            return (patient != null) ? List.of(patient.getPatientId()) : Collections.emptyList();
        }
        return Collections.emptyList();
    }

    private String resolveTopic(String hospitalName) {
        return hospitalName.toLowerCase().replaceAll("\\s+", "-") + "-response";
    }
}
