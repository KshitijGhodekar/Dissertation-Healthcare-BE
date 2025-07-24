package com.crossborder.hospitalA.service;

import com.crossborder.hospitalA.encryption.AESEncryptionUtil;
import com.crossborder.hospitalA.encryption.ECDSAUtil;
import com.crossborder.hospitalA.fabric.FabricClient;
import com.crossborder.hospitalA.model.AccessLog;
import com.crossborder.hospitalA.model.FabricLog;
import com.crossborder.hospitalA.model.PatientDataRequest;
import com.crossborder.hospitalA.model.PatientEntity;
import com.crossborder.hospitalA.repository.AccessLogRepository;
import com.crossborder.hospitalA.repository.FabricLogRepository;
import com.crossborder.hospitalA.repository.PatientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

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

        FabricClient fabricClient = null;
        boolean accessGranted = false;
        System.out.println("Incoming Request: " + request.toString());

        try {
            // 1. Check access with Fabric
            fabricClient = new FabricClient();
            accessGranted = fabricClient.isDoctorAuthorized(
                    request.getDoctorId(),
                    request.getPatientId(),
                    request.getPurpose(),
                    request.getHospitalName()
            );

            // 2. Save access log
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

            // 4. If denied, exit
            if (!accessGranted) {
                System.out.println("Access denied for Doctor ID: " + request.getDoctorId());
                return;
            }

            // 5. Fetch patient from DB
            PatientEntity patient = patientRepository.findByPatientId(request.getPatientId());
            if (patient == null) {
                System.out.println("Patient not found: " + request.getPatientId());
                return;
            }

            // 6. Build enriched FHIR-style JSON
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
            System.out.println("Enriched Patient JSON:");
            System.out.println(json);

            // 7. Encrypt with AES
            byte[] encrypted = AESEncryptionUtil.encryptToBytes(json);

            // 8. Sign encrypted data with ECDSA
            String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);
            String signature = ECDSAUtil.sign(encryptedBase64);

            // 9. Build Kafka message with signature (NO public key sent)
            ObjectNode signedPayload = mapper.createObjectNode();
            signedPayload.put("encryptedData", encryptedBase64);
            signedPayload.put("signature", signature);

            byte[] kafkaPayload = signedPayload.toString().getBytes(StandardCharsets.UTF_8);
            String topic = resolveTopic(request.getHospitalName());
            binaryKafkaTemplate.send(topic, request.getPatientId(), kafkaPayload);

            System.out.println("Signed + Encrypted data sent to topic: " + topic);

        } catch (Exception e) {
            System.err.println("Exception in processing request: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (fabricClient != null) {
                fabricClient.close();
            }
        }
    }

    private String resolveTopic(String hospitalName) {
        return hospitalName.toLowerCase().replaceAll("\\s+", "-") + "-response";
    }
}
