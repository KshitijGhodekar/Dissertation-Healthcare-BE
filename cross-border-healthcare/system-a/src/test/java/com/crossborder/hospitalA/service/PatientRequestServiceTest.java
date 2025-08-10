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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
        when(mockFuture.isDone()).thenReturn(true);
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(mockFuture);
    }

    @Test
    public void testProcessSinglePatientById() throws Exception {
        PatientDataRequest request = createTestRequest();
        request.setPatientId("P001");

        FabricResponse mockResponse = createMockFabricResponse(true);
        when(fabricClient.isDoctorAuthorizedDetailed(any(), any(), any(), any())).thenReturn(mockResponse);
        when(patientRepository.findByPatientId("P001")).thenReturn(createCompletePatient("P001"));

        patientRequestService.processRequest(request);

        verify(patientRepository).findByPatientId("P001");
        verify(kafkaTemplate).send(any(), any(), any());
        verify(fabricLogRepository).save(any(FabricLog.class));
        verify(accessLogRepository).save(any(AccessLog.class));
    }

    @Test
    public void testProcessPatientByMobileNumber() throws Exception {
        PatientDataRequest request = createTestRequest();
        request.setMobileNumber("1234567890");

        FabricResponse mockResponse = createMockFabricResponse(true);
        when(patientRepository.findByMobileNumber("1234567890")).thenReturn(createCompletePatient("P001"));
        when(fabricClient.isDoctorAuthorizedDetailed(any(), any(), any(), any())).thenReturn(mockResponse);
        when(patientRepository.findByPatientId("P001")).thenReturn(createCompletePatient("P001"));

        patientRequestService.processRequest(request);

        verify(patientRepository).findByMobileNumber("1234567890");
        verify(kafkaTemplate).send(any(), any(), any());
    }

    @Test
    public void testUnauthorizedAccess() throws Exception {
        PatientDataRequest request = createTestRequest();
        request.setPatientId("P001");

        FabricResponse mockResponse = createMockFabricResponse(false);
        when(fabricClient.isDoctorAuthorizedDetailed(any(), any(), any(), any())).thenReturn(mockResponse);

        patientRequestService.processRequest(request);

        verify(accessLogRepository).save(argThat(log -> !log.isAccessGranted()));
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    public void testPatientNotFound() throws Exception {
        PatientDataRequest request = createTestRequest();
        request.setPatientId("P999");

        FabricResponse mockResponse = createMockFabricResponse(true);
        when(fabricClient.isDoctorAuthorizedDetailed(any(), any(), any(), any())).thenReturn(mockResponse);
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

        verifyNoInteractions(kafkaTemplate, fabricClient);
    }

    @Test
    public void testPatientWithNullFields() throws Exception {
        PatientDataRequest request = createTestRequest();
        request.setPatientId("P001");

        PatientEntity patient = createCompletePatient("P001");
        patient.setDateOfAdmission(null);
        patient.setDischargeDate(null);

        FabricResponse mockResponse = createMockFabricResponse(true);
        when(fabricClient.isDoctorAuthorizedDetailed(any(), any(), any(), any())).thenReturn(mockResponse);
        when(patientRepository.findByPatientId("P001")).thenReturn(patient);

        patientRequestService.processRequest(request);

        verify(kafkaTemplate).send(any(), any(), any());
    }

    @Test
    public void testPayloadEncryptionAndSigning() throws Exception {
        PatientDataRequest request = createTestRequest();
        request.setPatientId("P001");

        PatientEntity patient = createCompletePatient("P001");
        FabricResponse mockResponse = createMockFabricResponse(true);
        when(fabricClient.isDoctorAuthorizedDetailed(any(), any(), any(), any())).thenReturn(mockResponse);
        when(patientRepository.findByPatientId("P001")).thenReturn(patient);

        patientRequestService.processRequest(request);

        ArgumentCaptor<byte[]> payloadCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(kafkaTemplate).send(any(), any(), payloadCaptor.capture());

        String payload = new String(payloadCaptor.getValue());
        assertTrue(payload.contains("encryptedData"));
        assertTrue(payload.contains("signature"));

        // Verify the payload can be parsed as JSON
        assertDoesNotThrow(() -> mapper.readTree(payload));
    }

    @Test
    public void testEmptyPatientIdsList() {
        PatientDataRequest request = createTestRequest();
        request.setPatientIds(Collections.emptyList());

        patientRequestService.processRequest(request);

        verifyNoInteractions(patientRepository, kafkaTemplate, fabricClient);
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

        PatientEntity patient = createCompletePatient("P001");
        FabricResponse mockResponse = createMockFabricResponse(true);
        when(fabricClient.isDoctorAuthorizedDetailed(any(), any(), any(), any())).thenReturn(mockResponse);
        when(patientRepository.findByPatientId("P001")).thenReturn(patient);

        patientRequestService.processRequest(request);

        verify(kafkaTemplate).send(eq("dublin-general-hospital-response"), any(), any());
    }

    private PatientDataRequest createTestRequest() {
        PatientDataRequest request = new PatientDataRequest();
        request.setDoctorId("D001");
        request.setDoctorName("Dr. Smith");
        request.setTimestamp(Instant.now().toString()); // Fixed timestamp format
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

    private FabricResponse createMockFabricResponse(boolean authorized) {
        FabricResponse response = new FabricResponse();
        response.setAccessGranted(authorized);
        response.setTransactionId("tx-" + UUID.randomUUID());
        response.setBlockNumber(1L);
        response.setValidationCode("VALID");
        response.setResponsePayload("{\"authorized\":" + authorized + "}");
        response.setInputArgsJson("[]");
        response.setEndorsersJson("[]");
        return response;
    }
}