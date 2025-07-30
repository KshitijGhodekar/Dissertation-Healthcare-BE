package com.crossborder.hospitalB.service;

import com.crossborder.hospitalB.encryption.AESEncryptionUtil;
import com.crossborder.hospitalB.encryption.ECDSAUtil;
import com.crossborder.hospitalB.model.PatientEntity;
import com.crossborder.hospitalB.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PatientResponseServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private EmailNotificationService emailService;

    @InjectMocks
    private PatientResponseService patientResponseService;

    private final String testRequestId = "test-req-123";
    private final String validJson = """
        {
            \"patientId\": \"P001\",
            \"name\": \"John Doe\",
            \"doctorName\": \"Dr. Smith\",
            \"age\": 35,
            \"gender\": \"Male\",
            \"bloodType\": \"O+\",
            \"medicalCondition\": \"Hypertension\",
            \"dateOfAdmission\": \"2025-01-15\",
            \"dischargeDate\": \"2025-01-20\",
            \"doctor\": \"Dr. Johnson\",
            \"hospital\": \"City General\",
            \"medication\": \"Lisinopril\",
            \"testResults\": \"Stable\",
            \"requestId\": \"%s\"
        }
        """.formatted(testRequestId);

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        AESEncryptionUtil.setKey("1234567890123456");
    }

    @Test
    void shouldProcessValidPatientData() throws Exception {
        byte[] encrypted = AESEncryptionUtil.encryptToBytes(validJson);
        String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);
        String signature = ECDSAUtil.sign(encryptedBase64);

        String kafkaMessage = """
            {
                \"encryptedData\": \"%s\",
                \"signature\": \"%s\",
                \"requestId\": \"%s\"
            }
            """.formatted(encryptedBase64, signature, testRequestId);

        when(patientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        patientResponseService.processPatientData(kafkaMessage.getBytes());

        verify(patientRepository).save(any(PatientEntity.class));
        verify(emailService).sendPatientDataPDF(
                eq("kshitijdghodekar@gmail.com"),
                eq("Dr. Smith"),
                any(PatientEntity.class),
                eq(true)
        );
    }

    @Test
    void shouldRejectTamperedData() throws Exception {
        byte[] encrypted = AESEncryptionUtil.encryptToBytes(validJson);
        String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);

        String kafkaMessage = """
            {
                \"encryptedData\": \"%s\",
                \"signature\": \"tampered_signature\",
                \"requestId\": \"%s\"
            }
            """.formatted(encryptedBase64, testRequestId);

        patientResponseService.processPatientData(kafkaMessage.getBytes());

        verify(patientRepository, never()).save(any());
        verify(emailService, never()).sendPatientDataPDF(any(), any(), any(), anyBoolean());
    }

    @Test
    void shouldHandleDecryptionErrors() throws Exception {
        String kafkaMessage = """
            {
                \"encryptedData\": \"invalid_encrypted_data\",
                \"signature\": \"fake_signature\",
                \"requestId\": \"%s\"
            }
            """.formatted(testRequestId);

        patientResponseService.processPatientData(kafkaMessage.getBytes());

        verify(patientRepository, never()).save(any());
        verify(emailService, never()).sendPatientDataPDF(any(), any(), any(), anyBoolean());
    }

    @Test
    void shouldHandleMalformedPatientData() throws Exception {
        String malformedJson = "{ invalid: json }";
        byte[] encrypted = AESEncryptionUtil.encryptToBytes(malformedJson);
        String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);
        String signature = ECDSAUtil.sign(encryptedBase64);

        String kafkaMessage = """
            {
                \"encryptedData\": \"%s\",
                \"signature\": \"%s\",
                \"requestId\": \"%s\"
            }
            """.formatted(encryptedBase64, signature, testRequestId);

        patientResponseService.processPatientData(kafkaMessage.getBytes());

        verify(patientRepository, never()).save(any());
        verify(emailService, never()).sendPatientDataPDF(any(), any(), any(), anyBoolean());
    }

    @Test
    void shouldCompleteProcessingWhenEmailFails() throws Exception {
        byte[] encrypted = AESEncryptionUtil.encryptToBytes(validJson);
        String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);
        String signature = ECDSAUtil.sign(encryptedBase64);

        String kafkaMessage = """
            {
                \"encryptedData\": \"%s\",
                \"signature\": \"%s\",
                \"requestId\": \"%s\"
            }
            """.formatted(encryptedBase64, signature, testRequestId);

        doThrow(new RuntimeException("SMTP error")).when(emailService)
                .sendPatientDataPDF(any(), any(), any(), anyBoolean());

        patientResponseService.processPatientData(kafkaMessage.getBytes());

        verify(patientRepository).save(any(PatientEntity.class));
    }

    private PatientEntity createTestPatient() {
        PatientEntity patient = new PatientEntity();
        patient.setPatientId("P001");
        patient.setName("John Doe");
        patient.setAge(35);
        patient.setGender("Male");
        patient.setBloodType("O+");
        patient.setMedicalCondition("Hypertension");
        patient.setDateOfAdmission(LocalDate.of(2025, 1, 15));
        patient.setDischargeDate(LocalDate.of(2025, 1, 20));
        patient.setDoctor("Dr. Johnson");
        patient.setHospital("City General");
        patient.setMedication("Lisinopril");
        patient.setTestResults("Stable");
        return patient;
    }
}