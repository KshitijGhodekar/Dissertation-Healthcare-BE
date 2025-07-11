package com.crossborder.hospitalB.service;

import com.crossborder.hospitalB.encryption.AESEncryptionUtil;
import com.crossborder.hospitalB.model.PatientEntity;
import com.crossborder.hospitalB.repository.PatientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientResponseService {

    @Autowired
    private PatientRepository patientRepository;

    public void processPatientData(byte[] encryptedData) {
        try {
            // Use the new decrypt method for raw byte[] input
            String decryptedJson = AESEncryptionUtil.decrypt(encryptedData);

            // Deserialize JSON into PatientEntity
            ObjectMapper mapper = new ObjectMapper();
            PatientEntity patient = mapper.readValue(decryptedJson, PatientEntity.class);

            // Save to DB
            patientRepository.save(patient);

            System.out.println("Patient data saved for: " + patient.getPatientId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
