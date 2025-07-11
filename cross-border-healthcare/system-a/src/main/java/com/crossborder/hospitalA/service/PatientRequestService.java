package com.crossborder.hospitalA.service;

import com.crossborder.hospitalA.encryption.AESEncryptionUtil;
import com.crossborder.hospitalA.fabric.FabricClient;
import com.crossborder.hospitalA.model.AccessLog;          // ✅ new
import com.crossborder.hospitalA.model.PatientDataRequest;
import com.crossborder.hospitalA.model.PatientEntity;
import com.crossborder.hospitalA.repository.AccessLogRepository; // ✅ new
import com.crossborder.hospitalA.repository.PatientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PatientRequestService {

    private static final String RESPONSE_TOPIC = "patient-data-response";

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AccessLogRepository accessLogRepository;     // ✅ new

    @Autowired
    private KafkaTemplate<String, byte[]> binaryKafkaTemplate;

    public void processRequest(PatientDataRequest request) {

        FabricClient fabricClient = null;
        boolean accessGranted = false;

        try {
            /* -------------------------------------------
               1️⃣  Verify access on-chain
            ------------------------------------------- */
            fabricClient = new FabricClient();
            accessGranted = fabricClient.isDoctorAuthorized(
                    request.getDoctorId(),
                    request.getPatientId(),
                    request.getPurpose(),
                    request.getHospitalName()              // 🔄 dynamic
            );

            /* -------------------------------------------
               2️⃣  Log the attempt in local DB (always)
            ------------------------------------------- */
            AccessLog log = new AccessLog();
            log.setDoctorId(request.getDoctorId());
            log.setPatientId(request.getPatientId());
            log.setPurpose(request.getPurpose());
            log.setHospitalName(request.getHospitalName());
            log.setTimestamp(request.getTimestamp());
            log.setAccessGranted(accessGranted);
            accessLogRepository.save(log);

            if (!accessGranted) {
                System.out.println("⛔ Access denied for Doctor ID: " + request.getDoctorId());
                return;
            }

            /* -------------------------------------------
               3️⃣  Fetch, encrypt & publish patient data
            ------------------------------------------- */
            PatientEntity patient = patientRepository.findByPatientId(request.getPatientId());
            if (patient == null) {
                System.out.println("❌ Patient not found: " + request.getPatientId());
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(patient);
            byte[] encrypted = AESEncryptionUtil.encryptToBytes(json);

            binaryKafkaTemplate.send(RESPONSE_TOPIC, request.getPatientId(), encrypted);
            System.out.println("✅ Access granted and data sent for: " + request.getPatientId());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fabricClient != null) {
                fabricClient.close();                     // 🔒 always close gateway
            }
        }
    }
}
