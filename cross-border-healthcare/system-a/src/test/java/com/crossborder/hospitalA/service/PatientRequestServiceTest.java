package com.crossborder.hospitalA.service;

import com.crossborder.hospitalA.encryption.AESEncryptionUtil;
import com.crossborder.hospitalA.fabric.FabricClient;
import com.crossborder.hospitalA.model.*;
import com.crossborder.hospitalA.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PatientRequestServiceTest {

    @InjectMocks
    private PatientRequestService patientRequestService;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AccessLogRepository accessLogRepository;

    @Mock
    private FabricLogRepository fabricLogRepository;

    @Mock
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @Mock
    private FabricClient fabricClient;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        patientRequestService.setFabricClient(fabricClient);

        // Properly mock KafkaTemplate.send() to return a dummy CompletableFuture
        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, byte[]>> mockFuture = mock(CompletableFuture.class);
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(mockFuture);
    }

//    @Test
//    public void testProcessMultiplePatientIds() throws Exception {
//        PatientDataRequest request = createTestRequest();
//        request.setPatientIds(List.of("P001", "P002"));
//
//        when(fabricClient.isDoctorAuthorized(any(), any(), any(), any())).thenReturn(true);
//        when(patientRepository.findByPatientId("P001")).thenReturn(createCompletePatient("P001"));
//        when(patientRepository.findByPatientId("P002")).thenReturn(createCompletePatient("P002"));
//
//        patientRequestService.processRequest(request);
//
//        verify(patientRepository, times(1)).findByPatientId("P001");
//        verify(patientRepository, times(1)).findByPatientId("P002");
//        verify(kafkaTemplate, times(2)).send(any(), any(), any());
//    }

    @Test
    public void testProcessSinglePatientById() throws Exception {
        PatientDataRequest request = createTestRequest();
        request.setPatientId("P001");

        when(fabricClient.isDoctorAuthorized(any(), any(), any(), any())).thenReturn(true);
        when(patientRepository.findByPatientId("P001")).thenReturn(createCompletePatient("P001"));

        patientRequestService.processRequest(request);

        verify(patientRepository).findByPatientId("P001");
        verify(kafkaTemplate).send(any(), any(), any());
    }

    @Test
    public void testProcessPatientByMobileNumber() throws Exception {
        PatientDataRequest request = createTestRequest();
        request.setMobileNumber("1234567890");

        when(patientRepository.findByMobileNumber("1234567890")).thenReturn(createCompletePatient("P001"));
        when(fabricClient.isDoctorAuthorized(any(), any(), any(), any())).thenReturn(true);
        when(patientRepository.findByPatientId("P001")).thenReturn(createCompletePatient("P001"));

        patientRequestService.processRequest(request);

        verify(patientRepository).findByMobileNumber("1234567890");
        verify(kafkaTemplate).send(any(), any(), any());
    }

    @Test
    public void testUnauthorizedAccess() throws Exception {
        PatientDataRequest request = createTestRequest();
        request.setPatientId("P001");

        when(fabricClient.isDoctorAuthorized(any(), any(), any(), any())).thenReturn(false);

        patientRequestService.processRequest(request);

        verify(accessLogRepository).save(argThat(log -> !log.isAccessGranted()));
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    public void testPatientNotFound() throws Exception {
        PatientDataRequest request = createTestRequest();
        request.setPatientId("P999");

        when(fabricClient.isDoctorAuthorized(any(), any(), any(), any())).thenReturn(true);
        when(patientRepository.findByPatientId("P999")).thenReturn(null);

        patientRequestService.processRequest(request);

        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    public void testMobileNumberNotFound() {
        PatientDataRequest request = createTestRequest();
        request.setMobileNumber("0000000000");

        when(patientRepository.findByMobileNumber("0000000000")).thenReturn(null);

        patientRequestService.processRequest(request);

        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    public void testPatientWithNullFields() throws Exception {
        PatientDataRequest request = createTestRequest();
        request.setPatientId("P001");

        PatientEntity patient = createCompletePatient("P001");
        patient.setDateOfAdmission(null);
        patient.setDischargeDate(null);

        when(fabricClient.isDoctorAuthorized(any(), any(), any(), any())).thenReturn(true);
        when(patientRepository.findByPatientId("P001")).thenReturn(patient);

        patientRequestService.processRequest(request);

        verify(kafkaTemplate).send(any(), any(), any());
    }

    @Test
    public void testPayloadEncryptionAndSigning() throws Exception {
        PatientDataRequest request = createTestRequest();
        request.setPatientId("P001");

        PatientEntity patient = createCompletePatient("P001");
        when(fabricClient.isDoctorAuthorized(any(), any(), any(), any())).thenReturn(true);
        when(patientRepository.findByPatientId("P001")).thenReturn(patient);

        patientRequestService.processRequest(request);

        ArgumentCaptor<byte[]> payloadCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(kafkaTemplate).send(any(), any(), payloadCaptor.capture());

        String payload = new String(payloadCaptor.getValue());
        assertTrue(payload.contains("encryptedData"));
        assertTrue(payload.contains("signature"));

        // AES decryption
        String encrypted = mapper.readTree(payload).get("encryptedData").asText();
        assertDoesNotThrow(() -> AESEncryptionUtil.decrypt(Base64.getDecoder().decode(encrypted)));
    }

    @Test
    public void testEmptyPatientIdsList() {
        PatientDataRequest request = createTestRequest();
        request.setPatientIds(Collections.emptyList());

        patientRequestService.processRequest(request);

        verifyNoInteractions(patientRepository, kafkaTemplate);
    }

    @Test
    public void testNullRequest() {
        assertThrows(IllegalArgumentException.class,
                () -> patientRequestService.processRequest(null));
    }

    @Test
    public void testHospitalNameWithSpaces() throws Exception {
        PatientDataRequest request = createTestRequest();
        request.setPatientId("P001");
        request.setHospitalName("Dublin General Hospital");

        when(fabricClient.isDoctorAuthorized(any(), any(), any(), any())).thenReturn(true);
        when(patientRepository.findByPatientId("P001")).thenReturn(createCompletePatient("P001"));

        patientRequestService.processRequest(request);

        verify(kafkaTemplate).send(eq("dublin-general-hospital-response"), any(), any());
    }

    private PatientDataRequest createTestRequest() {
        PatientDataRequest request = new PatientDataRequest();
        request.setDoctorId("D001");
        request.setDoctorName("Dr. Smith");
        request.setTimestamp("2025-07-24T12:00:00");
        request.setPurpose("treatment");
        request.setHospitalName("ireland-hospital");
        return request;
    }

    private PatientEntity createCompletePatient(String patientId) {
        PatientEntity patient = new PatientEntity();
        patient.setPatientId(patientId);
        patient.setName("Patient " + patientId);
        patient.setAge(30);
        patient.setGender("Female");
        patient.setBloodType("A+");
        patient.setMedicalCondition("Fever");
        patient.setDateOfAdmission(LocalDate.now());
        patient.setDischargeDate(LocalDate.now().plusDays(1));
        patient.setDoctor("Dr. Smith");
        patient.setHospital("ireland-hospital");
        patient.setMedication("Paracetamol");
        patient.setTestResults("Normal");
        patient.setMobileNumber("1234567890");
        return patient;
    }
}