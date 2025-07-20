package com.crossborder.hospitalA.service;

import com.crossborder.hospitalA.encryption.AESEncryptionUtil;
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
            // Verify access on-chain
            fabricClient = new FabricClient();
            accessGranted = fabricClient.isDoctorAuthorized(
                    request.getDoctorId(),
                    request.getPatientId(),
                    request.getPurpose(),
                    request.getHospitalName()
            );

            // Log access attempt
            AccessLog accessLog = new AccessLog();
            accessLog.setDoctorId(request.getDoctorId());
            accessLog.setDoctorName(request.getDoctorName());
            accessLog.setPatientId(request.getPatientId());
            accessLog.setPurpose(request.getPurpose());
            accessLog.setHospitalName(request.getHospitalName());
            accessLog.setTimestamp(request.getTimestamp());
            accessLog.setAccessGranted(accessGranted);
            accessLogRepository.save(accessLog);

            // Log to FabricLog table
            FabricLog fabricLog = new FabricLog();
            fabricLog.setDoctorId(request.getDoctorId());
            fabricLog.setDoctorName(request.getDoctorName());
            fabricLog.setPatientId(request.getPatientId());
            fabricLog.setStatus(accessGranted ? "granted" : "denied");
            fabricLog.setTimestamp(request.getTimestamp());
            fabricLogRepository.save(fabricLog);

            if (!accessGranted) {
                System.out.println("Access denied for Doctor ID: " + request.getDoctorId());
                return;
            }

            // Fetch patient record
            PatientEntity patient = patientRepository.findByPatientId(request.getPatientId());
            if (patient == null) {
                System.out.println("Patient not found: " + request.getPatientId());
                return;
            }

            // Build enriched JSON
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
            System.out.println("🔍 Sending enriched JSON:");
            System.out.println(json);

            // Optional: Skip FHIRValidator for now, or adapt if needed
            // FHIRValidator.validatePatient(new JSONObject(json));

            // Encrypt and send to Kafka
            byte[] encrypted = AESEncryptionUtil.encryptToBytes(json);
            String topic = resolveTopic(request.getHospitalName());
            binaryKafkaTemplate.send(topic, request.getPatientId(), encrypted);

            System.out.println("Encrypted data sent to topic: " + topic);

        } catch (Exception e) {
            System.err.println("Exception in processing request: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (fabricClient != null) {
                fabricClient.close();
            }
        }
    }

    // Dynamic topic resolution
    private String resolveTopic(String hospitalName) {
        return hospitalName.toLowerCase().replaceAll("\\s+", "-") + "-response";
    }
}
