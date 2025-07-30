package com.crossborder.hospitalA.service;

import com.crossborder.hospitalA.encryption.AESEncryptionUtil;
import com.crossborder.hospitalA.encryption.ECDSAUtil;
import com.crossborder.hospitalA.fabric.FabricClient;
import com.crossborder.hospitalA.model.*;
import com.crossborder.hospitalA.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class PatientRequestService {

    private static final Logger logger = LoggerFactory.getLogger(PatientRequestService.class);

    @Autowired private PatientRepository patientRepository;
    @Autowired private AccessLogRepository accessLogRepository;
    @Autowired private FabricLogRepository fabricLogRepository;
    @Autowired private KafkaTemplate<String, byte[]> binaryKafkaTemplate;
    @Autowired private FabricClient fabricClient;

    private final Set<String> processedKeys = Collections.synchronizedSet(new HashSet<>());

    public void processRequest(PatientDataRequest request) {
        if (request == null) throw new IllegalArgumentException("Request cannot be null");
        logger.info("Received new patient data request: {}", request.getRequestId());

        List<String> patientIds = resolvePatientIds(request);

        if (patientIds.isEmpty()) {
            logger.warn("No valid patient ID(s) found in the request.");
            return;
        }

        logger.info("Resolved {} patient ID(s) for processing: {}", patientIds.size(), patientIds);

        for (String pid : patientIds) {
            request.setPatientId(pid);
            processSinglePatient(request);
        }
    }

    private void processSinglePatient(PatientDataRequest request) {
        try {
            String dedupKey = request.getRequestId() + "-" + request.getPatientId();
            if (!processedKeys.add(dedupKey)) {
                logger.info("Duplicate request detected for: {} → Skipping.", dedupKey);
                return;
            }

            logger.info("Processing patient: {}", request.getPatientId());

            // 1. Verify access with detailed Fabric response
            logger.info("Verifying access with Fabric for doctor: {} and patient: {}", request.getDoctorId(), request.getPatientId());
            FabricResponse response = fabricClient.isDoctorAuthorizedDetailed(
                    request.getDoctorId(),
                    request.getPatientId(),
                    request.getPurpose(),
                    request.getHospitalName()
            );
            logger.info("Fabric verification completed. Access granted: {}", response.isAccessGranted());

            // 2. Log access attempt
            AccessLog accessLog = new AccessLog();
            accessLog.setDoctorId(request.getDoctorId());
            accessLog.setDoctorName(request.getDoctorName());
            accessLog.setPatientId(request.getPatientId());
            accessLog.setPurpose(request.getPurpose());
            accessLog.setHospitalName(request.getHospitalName());
            accessLog.setTimestamp(request.getTimestamp());
            accessLog.setAccessGranted(response.isAccessGranted());
            accessLogRepository.save(accessLog);
            logger.debug("Access log saved for patient: {}", request.getPatientId());

            // 3. Save Fabric log
            FabricLog fabricLog = new FabricLog();
            fabricLog.setDoctorId(request.getDoctorId());
            fabricLog.setDoctorName(request.getDoctorName());
            fabricLog.setPatientId(request.getPatientId());
            fabricLog.setStatus(response.isAccessGranted() ? "granted" : "denied");
            fabricLog.setTimestamp(request.getTimestamp());

            // Detailed Fabric fields
            fabricLog.setTransactionId(response.getTransactionId());
            fabricLog.setBlockNumber(response.getBlockNumber());
            fabricLog.setValidationCode(response.getValidationCode());
            fabricLog.setResponsePayload(response.getResponsePayload());
            fabricLog.setInputArgs(response.getInputArgsJson());
            fabricLog.setEndorsers(response.getEndorsersJson());

            fabricLogRepository.save(fabricLog);
            logger.debug("Fabric log saved for patient: {}", request.getPatientId());

            if (!response.isAccessGranted()) {
                logger.warn("Access denied for patient: {}", request.getPatientId());
                return;
            }

            // 4. Fetch patient entity
            PatientEntity patient = patientRepository.findByPatientId(request.getPatientId());
            if (patient == null) {
                logger.error("Patient not found: {}", request.getPatientId());
                return;
            }

            // 5. Convert to JSON (FHIR-like)
            logger.info("Constructing FHIR JSON for patient: {}", request.getPatientId());
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode patientJson = mapper.createObjectNode();
            patientJson.put("patientId", patient.getPatientId());
            patientJson.put("name", patient.getName());
            patientJson.put("doctorName", request.getDoctorName());
            patientJson.put("age", patient.getAge());
            patientJson.put("gender", patient.getGender());
            patientJson.put("bloodType", patient.getBloodType());
            patientJson.put("medicalCondition", patient.getMedicalCondition());
            patientJson.put("requestId", request.getRequestId());
            if (patient.getDateOfAdmission() != null) patientJson.put("dateOfAdmission", patient.getDateOfAdmission().toString());
            if (patient.getDischargeDate() != null) patientJson.put("dischargeDate", patient.getDischargeDate().toString());
            patientJson.put("doctor", patient.getDoctor());
            patientJson.put("hospital", patient.getHospital());
            patientJson.put("medication", patient.getMedication());
            patientJson.put("testResults", patient.getTestResults());

            String json = mapper.writeValueAsString(patientJson);

            // 6. Encrypt
            logger.info("Encrypting patient data for patientId: {}", request.getPatientId());
            String encryptedBase64 = AESEncryptionUtil.encrypt(json);
            logger.info("Patient data encrypted successfully for patientId: {}", request.getPatientId());

            // 7. Sign with ECDSA
            String signature = ECDSAUtil.sign(encryptedBase64);
            logger.info("ECDSA Signature generated for patient {}: {}", request.getPatientId(), signature);

            // 8. Publish to Kafka
            ObjectNode signedPayload = mapper.createObjectNode();
            signedPayload.put("encryptedData", encryptedBase64);
            signedPayload.put("signature", signature);

            byte[] kafkaPayload = signedPayload.toString().getBytes(StandardCharsets.UTF_8);
            String topic = resolveTopic(request.getHospitalName());

            logger.info("Publishing encrypted and signed data to Kafka topic: {}", topic);
            binaryKafkaTemplate.send(topic, request.getPatientId(), kafkaPayload);

        } catch (Exception e) {
            logger.error("Exception while processing patientId {}: {}", request.getPatientId(), e.getMessage(), e);
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
        String topic = hospitalName.toLowerCase().replaceAll("\\s+", "-") + "-response";
        logger.debug("Resolved Kafka topic for hospital {}: {}", hospitalName, topic);
        return topic;
    }

    public void setFabricClient(FabricClient fabricClient) {
        this.fabricClient = fabricClient;
    }
}
